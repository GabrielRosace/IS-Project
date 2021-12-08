const express = require('express')
const config = require('config')
const router = new express.Router()
const objectid = require('objectid')
const { google } = require('googleapis')
const googleEmail = config.get('google.email')
const googleKey = config.get('google.key')
const scopes = 'https://www.googleapis.com/auth/calendar'
const jwt = new google.auth.JWT(
  process.env[googleEmail],
  null,
  process.env[googleKey].replace(/\\n/g, '\n'),
  scopes
)
const nh = require('../helper-functions/notification-helpers')
const Image = require('../models/image')
const Member = require('../models/member')
const Group = require('../models/group')
const Activity = require('../models/activity')

const calendar = google.calendar({
  version: 'v3',
  auth: jwt
})

router.post('/:id/activities', async (req, res, next) => {
  if (!req.user_id) {
    return res.status(401).send('Not authenticated')
  }
  const user_id = req.user_id
  const group_id = req.params.id

  try {
    const { activity, events } = req.body
    const member = await Member.findOne({
      group_id,
      user_id,
      group_accepted: true,
      user_accepted: true
    })
    if (!member) {
      return res.status(401).send('Unauthorized')
    }
    if (!(events && activity)) {
      return res.status(400).send('Bad Request')
    }

    const activity_id = objectid()
    const image_id = objectid()
    const image = {
      image_id,
      owner_type: 'user',
      owner_id: user_id,
      url: 'https://avatars.dicebear.com/api/adventurer/dinosauro.svg',
      path: '/images/profiles/user_default_photo.png',
      thumbnail_path: '/images/profiles/user_default_photo.png'
    }
    activity.status = member.admin ? 'accepted' : 'pending'
    activity.activity_id = activity_id
    const group = await Group.findOne({ group_id })
    activity.group_name = group.name
    events.forEach(event => { event.extendedProperties.shared.activityId = activity_id })
    await Promise.all(
      events.map(event =>
        calendar.events.insert({
          calendarId: group.calendar_id,
          resource: event
        })
      )
    )
    await Image.create(image)
    await Activity.create(activity)
    if (member.admin) {
      await nh.newActivityNotification(group_id, user_id)
    }
    res.json({ status: activity.status })
  } catch (error) {
    next(error)
  }
})

module.exports = router
