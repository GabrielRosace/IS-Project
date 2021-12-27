const express = require('express')
const router = new express.Router()

const Partecipant = require('../models/partecipant')
const RecurringActivity = require('../models/recurring-activity')
const Recurrence = require('../models/recurrence')
const objectid = require('objectid')

// // TODO modificare come vengono passati i giorni dal client (con le [])
// // TODO aggiungere il campo nPart per dire quanti partecipanti partecipano ad un evento
router.post('/', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  let days = calculateDays(req.body.days)

  if (days.length === 0) return res.status(400).send('Days not found')

  RecurringActivity.findOne({ activity_id: req.body.activity_id }).exec().then((a) => {
    if (a) {
      Recurrence.findOne({ activity_id: a.activity_id }).exec().then((r) => {
        if(!checkDates(r, days)) return res.status(400).send('Incorrect days')
        Partecipant.findOne({ partecipant_id: userId, activity_id: a.activity_id }).exec().then((p) => {
          if (!p) {
            const newPartecipant = {
              partecipant_id: userId,
              activity_id: a.activity_id,
              days: days,
              service: false
            }
            try {
              Partecipant.create(newPartecipant).catch(error => {
                console.log(error)
              })
              return res.status(200).send('Partecipant created')
            } catch (error) {
              next(error)
            }
          } else {
            return res.status(400).send('Partecipant already exists')
          }
        })
      })
    } else {
      return res.status(400).send('Activity does not exist')
    }
  })
})

function calculateDays (reqDays) {
  let daysSplitted = reqDays.substring(1, reqDays.length - 1).split(',')
  let days = []

  for (let i = 0; i < daysSplitted.length; i++) {
    days.push(new Date(daysSplitted[i]))
  }
  return days
}

function checkDates (r, days) {
  let valid = false
  switch (r.type) {
    case 'daily':
      for (let i = 0; i < days.length; i++) {
        if (days[i] < r.start_date[0] || days[i] > r.end_date[0]) {
          return false
        }
      }
      break

    case 'weekly':
      for (let i = 0; i < days.length; i++) {
        valid = false
        for (let j = 0; j < r.start_date.length; j++) {
          if (days[i] < r.start_date[0] || days[i] > r.end_date[r.end_date.length - 1]) {
            return false
          }
          if (days[i].getDay() == r.start_date[j].getDay()) {
            valid = true
          }
        }
        if (!valid) return false
      }
      break

    case 'monthly':
      for (let i = 0; i < days.length; i++) {
        valid = false
        for (let j = 0; j < r.start_date.length; j++) {
          if (days[i].getDate() == r.start_date[j].getDate()) {
            valid = true
          }
          if (days[i] < r.start_date[0] || days[i] > r.end_date[r.end_date.length - 1]) return false
        }
        if (!valid) return false
      }
      break
  }
  return true
}

// Elimina una partecipazione
router.delete('/:activity_id', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  Partecipant.deleteOne({ activity_id: req.params.activity_id, partecipant_id: userId }).exec()
  return res.status(200).send('Partecipation deleted')
})

// Modifica una partecipazione
router.patch('/:activity_id', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  if (req.body.days) {
    let days = calculateDays(req.body.days)

    Recurrence.findOne({ activity_id: req.params.activity_id }).exec().then((r) => {
      if(!checkDates(r, days)) return res.status(400).send('Incorrect days')
      Partecipant.updateOne({ activity_id: r.activity_id, partecipant_id: userId }, { $set: { days: days } }).exec().then(() => {
        return res.status(200).send('Partecipation updated')
      })
    })
  }
})

// Ritorna il numero di partecipanti ad un evento
// ! modificata
router.get('/nPart/:activity_id', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  let activity_id = req.params.activity_id
  if (!activity_id) return res.status(400).send('Bad request')

  Partecipant.find({activity_id: activity_id}).exec().then(p => {
    return res.status(200).send(p.length.toString())
  }).catch(error => {
    return res.status(400).send('Error')
  })
})

router.get('/days/:activity_id', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  let activity_id = req.params.activity_id
  if (!activity_id) return res.status(400).send('Bad request')

  Partecipant.findOne({activity_id: activity_id}).exec().then((p) => {
    if(p) return res.status(200).send(p.days)
    else return res.status(400).send('Activity does not exist')
  })
})

module.exports = router
