const express = require('express')
const router = new express.Router()

// const Label = require('../models/label')
const Group = require('../models/group')
// const Member = require('../models/member')
// const Child = require('../models/child')
// const Parent = require('../models/parent')
const Image = require('../models/image')
const RecurringActivity = require('../models/recurring-activity')
const Recurrence = require('../models/recurrence')
const objectid = require('objectid')

// Crea una nuova attività ricorrente
router.post('/', (req, res, next) => {
    let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }

    Group.findOne({group_id: req.body.group_id, name: req.body.group_name}).exec().then((g) => {
        if(g){
            Image.findOne({image_id: req.body.image_id}).exec().then((i) => {
                if(i){
                    const { group_id, image_id, name, group_name, description, location, color } = req.body

                    const newActivity = {
                        group_id,
                        image_id,
                        name,
                        group_name,
                        description,
                        location,
                        color
                    }
                
                    newActivity.activity_id = objectid()
                    newActivity.creator_id = userId
                    newActivity.status = false

                    const { type, start_date, end_date } = req.body

                    const newRecurrence = {
                        type,
                        start_date,
                        end_date
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
                    res.status(400).send('Image not found')
                }
            })
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