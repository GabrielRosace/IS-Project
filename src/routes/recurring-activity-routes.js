/* eslint-disable no-tabs */
const express = require('express')
const router = new express.Router()

// const Label = require('../models/label')
const Group = require('../models/group')
// const Member = require('../models/member')
// const Child = require('../models/child')
// const Parent = require('../models/parent')
const Partecipant = require('../models/partecipant')
const Label = require('../models/label')
const RecurringActivity = require('../models/recurring-activity')
const Recurrence = require('../models/recurrence')
const objectid = require('objectid')
const { newExportEmail } = require('../helper-functions/export-activity-data')

// // TODO endpoint per avere eventi con lo le stesse label (stessi interessi)
// // TODO modifica le query per la ricerca degli oggetti
// // TODO aggiunta e eliminazione label da evento
// // TODO endpoint per nPart
// // TODO modificare get eventi filter by label
// // TODO endpoint per dire se un utente partecipa o meno ad un evento -> bool

// Crea una nuova attività ricorrente
// // TODO verificare che in caso di weekly e monthly i giorni siano rispettati
// // TODO group_name non serve
// // TODO controllare che con una ricorrenza daily ci sia solo una start_date e una sola end_date
router.post('/api/recurringActivity', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  Group.findOne({ group_id: req.body.group_id }).exec().then( async (g) => {
    if (g) {
      const { group_id, name, description, location, color } = req.body

      const newActivity = {
        group_id,
        name,
        description,
        location,
        color
      }

      newActivity.activity_id = objectid()
      newActivity.creator_id = userId
      newActivity.status = false
      newActivity.image_url = 'https://picsum.photos/200'

      let idLabels = req.body.labels.substring(1, req.body.labels.length - 1).split(',')
      let labels = []
      for(let i = 0; i < idLabels.length; i++){
        await Label.findOne({label_id: idLabels[i]}).exec().then(l => {
          labels.push(l.label_id)
        })
      }
      newActivity.labels = labels

      let start_date = startingDate(req.body.start_date)
      let end_date = endingDate(req.body.end_date)
      dateValidator(req.body.type, start_date, end_date, res)

      const newRecurrence = {
        type: req.body.type,
        start_date: start_date,
        end_date: end_date,
        service: false
      }

      newRecurrence.recurrence_id = objectid()
      newRecurrence.activity_id = newActivity.activity_id

      try {
        RecurringActivity.create(newActivity).then((a) => {
          Recurrence.create(newRecurrence).then(() => {
            res.status(200).send(a.activity_id)
          }).catch((error) => {
            console.log(error)
            a.remove()
            res.status(400).send('Impossible to create activity')
          })
        })
      } catch (error) {
        next(error)
      }
    } else {
      res.status(400).send('Group not found')
    }
  })
})

function startingDate (dateStart) {
  let start_dateSplitted = dateStart.substring(1, dateStart.length - 1).replace(/\s+/g, '')
  start_dateSplitted = start_dateSplitted.split(',')
  let start_date = []
  for (i = 0; i < start_dateSplitted.length; i++) {
    start_date.push(new Date(start_dateSplitted[i]))
  }
  return start_date
}

function endingDate (dateEnd) {
  let end_dateSplitted = dateEnd.substring(1, dateEnd.length - 1).replace(/\s+/g, '')
  end_dateSplitted = end_dateSplitted.split(',')
  let end_date = []
  for (i = 0; i < end_dateSplitted.length; i++) {
    end_date.push(new Date(end_dateSplitted[i]))
  }
  return end_date
}

function dateValidator (type, start_date, end_date, res) {
  if (type != 'daily' && type != 'weekly' && type != 'monthly') return res.status(400).send('Incorrect type')

  if (start_date.length != end_date.length) return res.status(400).send('Dates does not match')
  switch (type) {
    case 'daily':
      if (start_date.length > 1 || end_date.length > 1) return res.status(400).send('Incorrect dates')
      if (start_date[0] > end_date[0]) return res.status(400).send('Dates does not match')
      break
    case 'weekly':
      let start_tmp = new Date(start_date[0].toString())
      let start_nextMonday = start_tmp.getDate() + (8 - start_tmp.getDay())
      start_nextMonday = new Date(start_tmp.setDate(start_nextMonday))

      let end_tmp = new Date(end_date[0].toString())
      let end_nextMonday = end_tmp.getDate() + (8 - end_tmp.getDay())
      end_nextMonday = new Date(end_tmp.setDate(end_nextMonday))

      if (start_date[start_date.length - 1] > end_date[0]) return res.status(400).send('Incorrect dates')

      for (let i = 0; i < start_date.length; i++) {
        if (start_date[i].getDay() != end_date[i].getDay() || start_date[i] > end_date[i]) return res.status(400).send('Dates does not match')
        if (i < start_date.length - 1) {
          if (start_date[i] > start_date[i + 1] || start_date[i] >= start_nextMonday) return res.status(400).send('Dates does not match')
          if (end_date[i] > end_date[i + 1] || end_date[i] >= end_nextMonday) return res.status(400).send('Dates does not match')
        } else {
          if (start_date[i] >= start_nextMonday) return res.status(400).send('Dates does not match')
          if (end_date[i] >= end_nextMonday) return res.status(400).send('Dates does not match')
        }
      }

      break

    case 'monthly':
      if (start_date[start_date.length - 1] > end_date[0]) return res.status(400).send('Incorrect dates')

      for (let i = 0; i < end_date.length; i++) {
        if (start_date[i].getDate() != end_date[i].getDate() || start_date[i] > end_date[i]) return res.status(400).send('Dates does not match')
        if (i < start_date.length - 1) {
          if (start_date[i] > start_date[i + 1] || start_date[i].getMonth() != start_date[i + 1].getMonth()) return res.status(400).send('Dates does not match')
          if (end_date[i] > end_date[i + 1] || end_date[i].getMonth() != end_date[i + 1].getMonth()) return res.status(400).send('Dates does not match')
        }
      }
      break
  }
}

// Ritorna tutti gli eventi ricorrenti a cui un utente partecipa, secondo alcune caratteristiche, ad esempio tutti, solo quelli scaduti o solo quelli futuri
router.get('/partecipant/:group_id', async (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  let expired = req.query.expired
	if(!expired) return res.status(400).send('Bad request')

  let group_id = req.params.group_id
  if(!group_id) return res.status(400).send('Bad request')

	let result = []
	switch(expired){
		case 'none':
      Partecipant.aggregate([
        {
          '$match': {
            'service': false, 
            'partecipant_id': userId
          }
        }, {
          '$lookup': {
            'from': 'RecurringActivity', 
            'localField': 'activity_id', 
            'foreignField': 'activity_id', 
            'as': 'RecurringActivity'
          }
        }
      ]).then( async p => {
        if(p){
          for(let i = 0; i < p.length; i++){
            let activityId = p[i].RecurringActivity[0].activity_id
            await RecurringActivity.aggregate([
              {
                '$match': {
                  'activity_id': activityId
                }
              }, {
                '$lookup': {
                  'from': 'Label', 
                  'localField': 'labels', 
                  'foreignField': 'label_id', 
                  'as': 'labels'
                }
              }, {
                '$lookup': {
                  'from': 'Recurrence', 
                  'localField': 'activity_id', 
                  'foreignField': 'activity_id', 
                  'as': 'Recurrence'
                }
              }
            ]).then(a => {
              let groupId = a[0].group_id
              if(groupId == group_id){
                p[i].RecurringActivity[0] = a
                result.push(p[i])
              }
            })
          }
        }
        return res.status(200).json(p)
      }).catch(error => {
        return res.status(400).send('Error')
      })
			break
		case 'true':
      Partecipant.aggregate([
        {
          '$match': {
            'service': false, 
            'partecipant_id': userId
          }
        }, {
          '$lookup': {
            'from': 'RecurringActivity', 
            'localField': 'activity_id', 
            'foreignField': 'activity_id', 
            'as': 'RecurringActivity'
          }
        }
      ]).then(async p => {
        if(p){
          for(let i = 0; i < p.length; i++){
            let activityId = p[i].RecurringActivity[0].activity_id
            await RecurringActivity.aggregate([
              {
                '$match': {
                  'activity_id': activityId
                }
              }, {
                '$lookup': {
                  'from': 'Label', 
                  'localField': 'labels', 
                  'foreignField': 'label_id', 
                  'as': 'labels'
                }
              }, {
                '$lookup': {
                  'from': 'Recurrence', 
                  'localField': 'activity_id', 
                  'foreignField': 'activity_id', 
                  'as': 'Recurrence'
                }
              }
            ]).then(a => {
              let groupId = a[0].group_id
              if(groupId == group_id){
                let end_date = a[0].Recurrence[0].end_date
                if(end_date[end_date.length - 1] < new Date(Date.now())){
                  p[i].RecurringActivity[0] = a
                  result.push(p[i])
                }
              }
            })
          }
        }
        return res.status(200).json(result)
      }).catch(error => {
        console.log(error);
        return res.status(400).send('Error')
      })
			break
		case 'false':
      Partecipant.aggregate([
        {
          '$match': {
            'service': false, 
            'partecipant_id': userId
          }
        }, {
          '$lookup': {
            'from': 'RecurringActivity', 
            'localField': 'activity_id', 
            'foreignField': 'activity_id', 
            'as': 'RecurringActivity'
          }
        }
      ]).then(async p => {
        if(p){
          for(let i = 0; i < p.length; i++){
            let activityId = p[i].RecurringActivity[0].activity_id
            await RecurringActivity.aggregate([
              {
                '$match': {
                  'activity_id': activityId
                }
              }, {
                '$lookup': {
                  'from': 'Label', 
                  'localField': 'labels', 
                  'foreignField': 'label_id', 
                  'as': 'labels'
                }
              }, {
                '$lookup': {
                  'from': 'Recurrence', 
                  'localField': 'activity_id', 
                  'foreignField': 'activity_id', 
                  'as': 'Recurrence'
                }
              }
            ]).then(a => {
              let groupId = a[0].group_id
              if(groupId == group_id){
                let end_date = a[0].Recurrence[0].end_date
                if(end_date[end_date.length - 1] > new Date(Date.now()))
                  p[i].RecurringActivity[0] = a
                  result.push(p[i])
              }
            })
          }
        }
        return res.status(200).json(result)
      }).catch(error => {
        console.log(error);
        return res.status(400).send('Error')
      })
			break
		default:
			return res.status(400).send('Bad request')
			break
	}

  // RecurringActivity.find({ group_id: group_id }).exec().then((a) => {
  //   return res.status(200).send(a)
  // })
})

// Ritorna tutti gli eventi ricorrenti che sono stati creati da un utente: tutti, quelli scaduti o quelli futuri
router.get('/creator/:group_id', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  let expired = req.query.expired
	if(!expired) return res.status(400).send('Bad request')

  let group_id = req.params.group_id
  if(!group_id) return res.status(400).send('Bad request')

  let result = []
  switch(expired){
    case 'none':
      RecurringActivity.aggregate([
        {
          '$match': {
            'creator_id': userId,
            'group_id': group_id
          }
        }, {
          '$lookup': {
            'from': 'Label', 
            'localField': 'labels', 
            'foreignField': 'label_id', 
            'as': 'labels'
          }
        }, {
          '$lookup': {
            'from': 'Recurrence', 
            'localField': 'activity_id', 
            'foreignField': 'activity_id', 
            'as': 'Recurrence'
          }
        }
      ]).then(a => {
        return res.status(200).json(a)
      }).catch(error => {
        return res.status(400).send('Error')
      })
      break
    case 'true':
      RecurringActivity.aggregate([
        {
          '$match': {
            'creator_id': userId,
            'group_id': group_id
          }
        }, {
          '$lookup': {
            'from': 'Label', 
            'localField': 'labels', 
            'foreignField': 'label_id', 
            'as': 'labels'
          }
        }, {
          '$lookup': {
            'from': 'Recurrence', 
            'localField': 'activity_id', 
            'foreignField': 'activity_id', 
            'as': 'Recurrence'
          }
        }
      ]).then(a => {
        for(let i = 0; i < a.length; i++){
          let end_dates = a[i].Recurrence[0].end_date
          if(end_dates[end_dates.length - 1] < new Date(Date.now()))
            result.push(a[i])
        }
        return res.status(200).json(result)
      }).catch(error => {
        console.log(error);
        return res.status(400).send('Error')
      })
      break
    case 'false':
      RecurringActivity.aggregate([
        {
          '$match': {
            'creator_id': userId,
            'group_id': group_id
          }
        }, {
          '$lookup': {
            'from': 'Label', 
            'localField': 'labels', 
            'foreignField': 'label_id', 
            'as': 'labels'
          }
        }, {
          '$lookup': {
            'from': 'Recurrence', 
            'localField': 'activity_id', 
            'foreignField': 'activity_id', 
            'as': 'Recurrence'
          }
        }
      ]).then(a => {
        for(let i = 0; i < a.length; i++){
          console.log(i);
          let end_dates = a[i].Recurrence[0].end_date
          if(end_dates[end_dates.length - 1] >= new Date(Date.now()))
            result.push(a[i])
        }
        // console.log(result);
        return res.status(200).json(result)
      }).catch(error => {
        console.log();
        return res.status(400).send('Error')
      })
      break
    default:
      return res.status(400).send('Bad request')
  }
})

// Ritorna tutte le informazioni di un evento ricorrente
router.get('/:activity_id', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  let activity_id = req.params.activity_id
  if (!activity_id) return res.status(400).send('Bad Request')

  // RecurringActivity.findOne({ activity_id: activity_id }).exec().then((a) => {
  //   return res.status(200).send(a)
  // })
  RecurringActivity.aggregate([
    {
      '$lookup': {
        'from': 'Label', 
        'localField': 'labels', 
        'foreignField': 'label_id', 
        'as': 'Label'
      }
    }
  ]).then(a => {
    return res.status(200).json(a)
  })
})

// Elimina un evento ricorrente
router.delete('/:activity_id', (req, res, user) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  Partecipant.deleteMany({ activity_id: req.params.activity_id }).catch(error => {
    console.log('Deleting error')
  })

  Recurrence.deleteMany({ activity_id: req.params.activity_id }).catch(error => {
    console.log('Deleting error')
  })

  RecurringActivity.deleteOne({ activity_id: req.params.activity_id }).catch(error => {
    console.log('Deleting error')
  })
  return res.status(200).send('Event deleted')
})

// Modifica le informazioni di un evento ricorrente
router.put('/:activity_id', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  RecurringActivity.findOne({ activity_id: req.params.activity_id }).exec().then(a => {
    if (a) {
      a.description = req.body.description ? req.body.description : a.description
      if (req.body.start_date && req.body.end_date && req.body.date_type) {
        Recurrence.deleteOne({ activity_id: a.activity_id }).then().catch(error => {
          console.log(error)
        })

        if (req.body.date_type != 'daily' && req.body.date_type != 'weekly' && req.body.date_type != 'monthly') return res.status(400).send('Incorrect type')

        // let start_dateSplitted = req.body.start_date.substring(1,req.body.start_date.length-1).replace(/\s+/g, '')
        // start_dateSplitted = start_dateSplitted.split(',')
        // let start_date = []
        // for(i = 0; i < start_dateSplitted.length; i++){
        //     start_date.push(new Date(start_dateSplitted[i]))
        // }
        let start_date = startingDate(req.body.start_date)

        // let end_dateSplitted = req.body.end_date.substring(1,req.body.end_date.length-1).replace(/\s+/g, '')
        // end_dateSplitted = end_dateSplitted.split(',')
        // let end_date = []
        // for(i = 0; i < end_dateSplitted.length; i++){
        //     end_date.push(new Date(end_dateSplitted[i]))
        // }
        let end_date = endingDate(req.body.end_date)

        dateValidator(req.body.date_type, start_date, end_date, res)

        // if(start_date.length != end_date.length) return res.status(400).send('Dates does not match')
        // switch(req.body.type){
        //     case 'daily':
        //         if(start_date.length > 1 || end_date.length > 1) return res.status(400).send('Incorrect dates')
        //         if(start_date[0] > end_date[0]) return res.status(400).send('Dates does not match')
        //         break
        //     case 'weekly':
        //         let start_tmp = new Date(start_date[0].toString())
        //         let start_nextMonday = start_tmp.getDate() + (8 - start_tmp.getDay())
        //         start_nextMonday = new Date(start_tmp.setDate(start_nextMonday))

        //         let end_tmp = new Date(end_date[0].toString())
        //         let end_nextMonday = end_tmp.getDate() + (8 - end_tmp.getDay())
        //         end_nextMonday = new Date(end_tmp.setDate(end_nextMonday))

        //         if(start_date[start_date.length - 1] > end_date[0]) return res.status(400).send('Incorrect dates')

        //         for(let i = 0; i < start_date.length; i++){
        //             if(start_date[i].getDay() != end_date[i].getDay() || start_date[i] > end_date[i]) return res.status(400).send('Dates does not match')
        //             if(i < start_date.length - 1){
        //                 if(start_date[i] > start_date[i + 1] || start_date[i] >= start_nextMonday) return res.status(400).send('Dates does not match')
        //                 if(end_date[i] > end_date[i + 1] || end_date[i] >= end_nextMonday) return res.status(400).send('Dates does not match')
        //             }
        //             else{
        //                 if(start_date[i] >= start_nextMonday) return res.status(400).send('Dates does not match')
        //                 if(end_date[i] >= end_nextMonday) return res.status(400).send('Dates does not match')
        //             }
        //         }
        //         break

        //     case 'monthly':
        //         if(start_date[start_date.length - 1] > end_date[0]) return res.status(400).send('Incorrect dates')

        //         for(let i = 0; i < end_date.length; i++){
        //             if(start_date[i].getDate() != end_date[i].getDate() || start_date[i] > end_date[i]) return res.status(400).send('Dates does not match')
        //             if(i < start_date.length - 1){
        //                 if(start_date[i] > start_date[i + 1] || start_date[i].getMonth() != start_date[i + 1].getMonth()) return res.status(400).send('Dates does not match')
        //                 if(end_date[i] > end_date[i + 1] || end_date[i].getMonth() != end_date[i + 1].getMonth()) return res.status(400).send('Dates does not match')
        //             }
        //         }
        //         break
        // }

        const newRecurrence = {
          type: req.body.date_type,
          start_date: start_date,
          end_date: end_date,
          service: false
        }

        newRecurrence.recurrence_id = objectid()
        newRecurrence.activity_id = a.activity_id

        try {
          Recurrence.create(newRecurrence)
        } catch (error) {
          console.log(error)
          next(error)
        }
      }
      a.save().then(() => {
        return res.status(200).send('Activity updated')
      }).catch(error => {
        console.log('Update error')
        console.log(error)
      })
    } else {
      return res.status(400).send('Activity does not exist')
    }
  })
})

// Aggiunge un etichetta esistente ad un'attività esistente
// ? Controllare se l'etichetta appartiene al gruppo dell'evento
router.patch('/label/:activity_id', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  let activity_id = req.params.activity_id
  if(!activity_id) return res.status(400).send('Bad Request')

  let label_id = req.body.label_id
  if(!label_id) return res.status(400).send('Bad Request')

  RecurringActivity.findOne({activity_id: activity_id}).exec().then(a => {
    if(a){
      Label.findOne({label_id: label_id}).exec().then(l => {
        if(l){
          a.labels.push(l.label_id)
          a.save().then(() => {
            return res.status(200).send('Label added')
          }).catch(error => {
            console.log(error);
            return res.status(400).send('Label not added')
          })
        }
        else{
          return res.status(400).send('Label does not exist')
        }
      })
    }
    else{
      return res.status(400).send('Activity does not exist')
    }
  })
})

// Elimina un'etichetta associata ad un evento da quest'ultimo
router.delete('/label/:activity_id/:label_id', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  let activity_id = req.params.activity_id
  if(!activity_id) return res.status(400).send('Bad Request')

  let label_id = req.params.label_id
  if(!label_id) return res.status(400).send('Bad Request')

  // RecurringActivity.findOne({activity_id: activity_id}).exec().then(a => {
  //   if(a){
  //     console.log(a.labels);
  //     for(let i = 0; i < a.labels.length; i++){
  //       if(a.labels[i] == label_id)
  //         delete a.labels[i]
  //     }
  //     console.log(a.labels);
  //     a.save().then(() => {
  //       return res.status(200).send('Label deleted')
  //     }).catch(error => {
  //       return res.status(400).send("Can't delete label")
  //     })
  //   }
  //   else{
  //     return res.status(400).send('Activity does not exist')
  //   }
  // })
  RecurringActivity.updateOne({activity_id: activity_id}, {$pull: {labels: label_id}}).then(() => {
    return res.status(200).send('Label deleted')
  }).catch(error => {
    return res.status(400).send("Can't delete label")
  })
})

// Ritorna tutte le attività con la stessa label
router.get('/label/:label_id', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  let labelId = req.params.label_id
  if (!labelId) return res.status(400).send('Bad request')

  RecurringActivity.aggregate([
    {
      '$match': {
        'labels': label_id
      }
    }, {
      '$lookup': {
        'from': 'Label', 
        'localField': 'labels', 
        'foreignField': 'label_id', 
        'as': 'Label'
      }
    }, {
      '$lookup': {
        'from': 'Recurrence', 
        'localField': 'activity_id', 
        'foreignField': 'activity_id', 
        'as': 'Recurrence'
      }
    }
  ]).then(a => {
    return res.status(200).json(a)
  }).catch(error => {
    return res.status(400).send('Error')
  })
})

router.get('/isPartecipant/:activity_id', (req, res, next) => {
  let userId = req.user_id
  if (!userId) { return res.status(401).send('Not authenticated') }

  let activity_id = req.params.activity_id
  if (!activity_id) return res.status(400).send('Bad request')

  Partecipant.findOne({activity_id: activity_id, partecipant_id: userId}).exec().then(p => {
    if(p)
      return res.status(200).send(true)
    else
      return res.status(200).send(false)
  })
})

module.exports = router
