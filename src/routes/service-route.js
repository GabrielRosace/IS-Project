/* eslint-disable no-unused-vars */
const express = require('express')
const router = new express.Router()

const Profile = require('../models/profile')
const Group = require('../models/group')
const Member = require('../models/member')
const User = require('../models/user')
const Parent = require('../models/parent')
const Child = require('../models/child')

const objectid = require('objectid')
const Service = require('../models/service')

router.delete('/:serviceId', async (req, res, next) => {
  if (!req.user_id) {
    return res.status(401).send('Not authenticated')
  }
  try {
    const service_id = req.params.serviceId
    const service = await Service.findOne({ service_id: service_id })
    if (!service) {
      return res.status(404).send('Service dont exist')
    }
    const group_id = service.group_id
    const user_id = req.user_id
    const member = await Member.findOne({
      group_id,
      user_id,
      group_accepted: true,
      user_accepted: true
    })
    if (!member) {
      return res.status(401).send('Unauthorized')
    }
    if (!member.admin) {
      return res.status(401).send('Unauthorized')
    }
    // delete recurrance
    await Service.findOneAndDelete({ service_id: service_id })

    res.status(200).send('Service deleted')
  } catch (error) {
    next(error)
  }
})

router.post('/', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  Group.findOne({ group_id: req.body.group_id }).exec().then((g) => {
    if (g) {
      const { group_id, name, description, location, pattern, car_space, lend_obj, lend_time, pickuplocation, img, recurrence } = req.body
      console.log(name)
      const newService = {
        group_id,
        name,
        img,
        description,
        location,
        pattern,
        recurrence: JSON.parse(JSON.stringify(recurrence))
      }

      newService.service_id = objectid()
      newService.owner_id = userId
      switch (pattern) {
        case 'car':
          newService.car_space = car_space
          break
        case 'lend':
          newService.lend_obj = lend_obj
          newService.lend_time = lend_time
          break
        case 'pickup':
          newService.pickuplocation = pickuplocation
          break
      }
      //  MANCA RECURRING DATE
      try {
        Service.create(newService).then(() => {
          res.status(200).send({ service_id: newService.service_id })
        }).catch((error) => {
          console.log(error)
          res.status(400).send('Impossible to create service')
        })
      } catch (error) {
        next(error)
      }
    } else {
      res.status(400).send('Group not found')
    }
  })
})

module.exports = router
