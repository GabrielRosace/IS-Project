const express = require('express')
const router = new express.Router()

const Partecipant = require('../models/partecipant')
const RecurringActivity = require('../models/recurring-activity')
const Recurrence = require('../models/recurrence')
const objectid = require('objectid')

// TODO modificare come vengono passati i giorni dal client (con le [])
router.post('/', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  let days = calculateDays(req.body.days)

  if (days.length === 0) return res.status(400).send('Days not found')

  RecurringActivity.findOne({ activity_id: req.body.activity_id }).exec().then((a) => {
    if (a) {
      Recurrence.findOne({ activity_id: a.activity_id }).exec().then((r) => {
        if(!checkDates(r, days, res)) return res.status(400).send('Incorrect days')
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
  let daysSplitted = reqDays ? reqDays.split(',') : undefined
  let days = []

  for (let i = 0; i < daysSplitted.length; i++) {
    days.push(new Date(daysSplitted[i]))
  }
  return days
}

function checkDates (r, days, res) {
  let valid = false
  switch (r.type) {
    case 'daily':
      for (let i = 0; i < days.length; i++) {
        if (days[i] < r.start_date[0] || days[i] > r.end_date[0]) {
          // return res.status(400).send('Incorrect days')
          return false
        }
      }
      break

    case 'weekly':
      valid = false
      for (let i = 0; i < days.length; i++) {
        for (let j = 0; j < r.start_date.length; j++) {
          if (days[i] < r.start_date[0] || days[i] > r.end_date[r.end_date.length - 1]) {
            // return res.status(400).send('Incorrect days')
            return false
          }
          if (days[i].getDay() == r.start_date[j].getDay()) {
            valid = true
          }
        }
        if (!valid) return false// res.status(400).send('Incorrect days')
      }
      break

    case 'monthly':
      valid = false
      for (let i = 0; i < days.length; i++) {
        for (let j = 0; j < r.start_date.length; j++) {
          if (days[i].getDate() == r.start_date[j].getDate()) {
            valid = true
          }
          if (!valid) return false// res.status(400).send('Incorrect days')
          if (days[i] < r.start_date[0] || days[i] > r.end_date[r.end_date.length - 1]) return false //res.status(400).send('Incorrect days')
        }
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
    // let daysSplitted = req.body.days ? req.body.days.split(',') : undefined
    // let days = []

    // for(let i = 0; i < daysSplitted.length; i++){
    //     days.push(new Date(daysSplitted[i]))
    // }
    let days = calculateDays(req.body.days)

    Recurrence.findOne({ activity_id: req.params.activity_id }).exec().then((r) => {
      checkDates(r, days, res)
      // let valid = false
      // switch(r.type){
      //     case 'daily':
      //         for(let i = 0; i < days.length; i++){
      //             if(days[i] < r.start_date[0] || days[i] > r.end_date[0]){
      //                 return res.status(400).send('Incorrect days')
      //             }
      //         }
      //         break

      //     case 'weekly':
      //         valid = false
      //         for(let i = 0; i < days.length; i++){
      //             for(let j = 0; j < start_date.length; j++){
      //                 if(days[i] < r.start_date[0] || days[i] > r.end_date[end_date.length - 1]){
      //                     return res.status(400).send('Incorrect days')
      //                 }
      //                 if(days[i].getDay() == r.start_date[j].getDay()){
      //                     valid = true
      //                 }
      //                 if(!valid) return res.status(400).send('Incorrect days')
      //             }
      //         }
      //         break

      //     case 'monthly':
      //         valid = false
      //         for(let i = 0; i < days.length; i++){
      //             for(let j = 0; j < r.start_date.length; j++){
      //                 console.log(days[i].getDate());
      //                 console.log(r.start_date[j].getDate());
      //                 if(days[i].getDate() == r.start_date[j].getDate()){
      //                     valid = true
      //                 }
      //                 if(days[i] < r.start_date[0] || days[i] > r.end_date[r.end_date.length - 1]) return res.status(400).send('Incorrect days2')
      //             }
      //         }
      //         if(!valid) return res.status(400).send('Incorrect days1')
      //         break
      // }

      Partecipant.updateOne({ activity_id: r.activity_id, partecipant_id: userId }, { $set: { days: days } }).exec().then(() => {
        return res.status(200).send('Partecipation updated')
      })
    })
  }
})

module.exports = router
