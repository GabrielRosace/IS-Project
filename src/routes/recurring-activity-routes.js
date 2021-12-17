const express = require('express')
const router = new express.Router()

// const Label = require('../models/label')
const Group = require('../models/group')
// const Member = require('../models/member')
// const Child = require('../models/child')
// const Parent = require('../models/parent')
const Partecipant = require('../models/partecipant')
const Image = require('../models/image')
const RecurringActivity = require('../models/recurring-activity')
const Recurrence = require('../models/recurrence')
const objectid = require('objectid')
const { newExportEmail } = require('../helper-functions/export-activity-data')

// TODO endpoint per avere eventi con lo le stesse label (stessi interessi)

// Crea una nuova attività ricorrente
// // TODO verificare che in caso di weekly e monthly i giorni siano rispettati
// // TODO group_name non serve
router.post('/', (req, res, next) => {
    let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }

    Group.findOne({group_id: req.body.group_id, name: req.body.group_name}).exec().then((g) => {
        if(g){
            const { group_id, image_url, name, group_name, description, location, color } = req.body

            const newActivity = {
                group_id,
                image_url,
                name,
                // group_name,
                description,
                location,
                color
            }
        
            newActivity.activity_id = objectid()
            newActivity.creator_id = userId
            newActivity.status = false

            if(req.body.type != 'daily' && req.body.type != 'weekly' && req.body.type != 'monthly') return res.status(400).send('Incorrect type')

            let start_dateSplitted = req.body.start_date.substring(1,req.body.start_date.length-1).replace(/\s+/g, '')
            start_dateSplitted = start_dateSplitted.split(',')
            let start_date = []
            for(i = 0; i < start_dateSplitted.length; i++){
                start_date.push(new Date(start_dateSplitted[i]))
            }

            let end_dateSplitted = req.body.end_date.substring(1,req.body.end_date.length-1).replace(/\s+/g, '')
            end_dateSplitted = end_dateSplitted.split(',')
            let end_date = []
            for(i = 0; i < end_dateSplitted.length; i++){
                end_date.push(new Date(end_dateSplitted[i]))
            }

			if(start_date.length != end_date.length) return res.status(400).send('Dates does not match')
			switch(req.body.type){
				case 'daily':
					if(start_date[0] > end_date[0]) return res.status(400).send('Dates does not match')
					break
                case 'weekly':
					let start_tmp = new Date(start_date[0].toString())
					let start_nextMonday = start_tmp.getDate() + (8 - start_tmp.getDay())
					start_nextMonday = new Date(start_tmp.setDate(start_nextMonday))

					let end_tmp = new Date(end_date[0].toString())
					let end_nextMonday = end_tmp.getDate() + (8 - end_tmp.getDay())
					end_nextMonday = new Date(end_tmp.setDate(end_nextMonday))

					if(start_date[start_date.length - 1] > end_date[0]) return res.status(400).send('Incorrect dates')

					for(let i = 0; i < start_date.length; i++){
						if(start_date[i].getDay() != end_date[i].getDay() || start_date[i] > end_date[i]) return res.status(400).send('Dates does not match')
						if(i < start_date.length - 1){
							if(start_date[i] > start_date[i + 1] || start_date[i] >= start_nextMonday) return res.status(400).send('Dates does not match')
							if(end_date[i] > end_date[i + 1] || end_date[i] >= end_nextMonday) return res.status(400).send('Dates does not match')
						}
						else{
							if(start_date[i] >= start_nextMonday) return res.status(400).send('Dates does not match')
							if(end_date[i] >= end_nextMonday) return res.status(400).send('Dates does not match')
						}
					}

                    break

                case 'monthly':
					if(start_date[start_date.length - 1] > end_date[0]) return res.status(400).send('Incorrect dates')

					for(let i = 0; i < end_date.length; i++){
						if(start_date[i].getDate() != end_date[i].getDate() || start_date[i] > end_date[i]) return res.status(400).send('Dates does not match')
						if(i < start_date.length - 1){
							if(start_date[i] > start_date[i + 1] || start_date[i].getMonth() != start_date[i + 1].getMonth()) return res.status(400).send('Dates does not match')
							if(end_date[i] > end_date[i + 1] || end_date[i].getMonth() != end_date[i + 1].getMonth()) return res.status(400).send('Dates does not match')
						}
					}
					break
            }

            const newRecurrence = {
                type: req.body.type,
                start_date: start_date,
                end_date: end_date,
                service: true
            }

            newRecurrence.recurrence_id = objectid()
            newRecurrence.activity_id = newActivity.activity_id
        
            try{
                RecurringActivity.create(newActivity).then((a) => {
                    Recurrence.create(newRecurrence).then(() => {
                        res.status(200).send('Activity created')
                    }).catch((error) => {
                        console.log(error);
                        a.remove()
                        res.status(400).send('Impossible to create activity')
                    })
                })
            }
            catch(error){
                next(error)
            }
        }
        else{
            res.status(400).send('Group not found')
        }
    })
})

// Ritorna tutti gli eventi ricorrenti
router.get('/:group_id', (req, res, next) => {
    let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }

    let group_id = req.params.group_id
	if(!group_id) return res.status(400).send('Bad Request')

    RecurringActivity.find({group_id: group_id}).exec().then((a) => {
        return res.status(200).send(a)
    })
})

// Ritorna tutte le informazioni di un evento ricorrente
router.get('/:activity_id', (req, res, next) => {
	let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }

    let activity_id = req.params.activity_id
	if(!activity_id) return res.status(400).send('Bad Request')

    RecurringActivity.findOne({activity_id: activity_id}).exec().then((a) => {
        return res.status(200).send(a)
    })
})

module.exports = router