const express = require('express')
const router = new express.Router()

const Partecipant = require('../models/partecipant')
const RecurringActivity = require('../models/recurring-activity')
const Recurrence = require('../models/recurrence')
const objectid = require('objectid')

router.post('/', (req, res, next) => {
    let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }

    let daysSplitted = req.body.days ? req.body.days.split(',') : undefined
    let days = []
    daysSplitted.forEach( element => {
        days.push(new Date(element))
    })

    if(days.length === 0) return res.status(400).send('Days not found')

    RecurringActivity.findOne({activity_id: req.body.activity_id}).exec().then((a) => {
        if(a){
            Recurrence.findOne({activity_id: a.activity_id}).exec().then((r) => {
                switch(r.type){
                    case 'daily':
                        for(let i = 0; i < days.length; i++){
                            if(days[i] < r.start_date || days[i] > r.end_date){
                                return res.status(400).send('Incorrect days')
                            }
                        }
                        break
                    
                    case 'weekly':
                        for(let i = 0; i < days.length; i++){
                            if(days[i] < r.start_date || days[i] > r.end_date || days[i].getDay() != r.start_date.getDay()){
                                return res.status(400).send('Incorrect days')
                            }
                        }
                        break

                    case 'monthly':
                        for(let i = 0; i < days.length; i++){
                            if(days[i].getDate() != r.start_date.getDate() || days[i] < r.start_date || days[i] > r.end_date){
                                return res.status(400).send('Incorrect days')
                            }
                        }
                        break
                }
                Partecipant.findOne({partecipant_id: userId, activity_id: a.activity_id}).exec().then((p) => {
                    if(!p){
                        const newPartecipant = {
                            partecipant_id: userId,
                            activity_id: a.activity_id,
                            days: days
                        }
                        try{
                            Partecipant.create(newPartecipant)
                            return res.status(200).send('Partecipant created')
                        }
                        catch(error){
                            next(error)
                        }
                    }
                    else{
                        return res.status(400).send('Partecipant already exists')
                    }
                })
            })
        }
        else{
            return res.status(400).send('Activity does not exist')
        }
    })
})

module.exports = router