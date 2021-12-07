const express = require('express')
const router = new express.Router()

const Label = require('../models/label')
const Group = require('../models/group')
const Member = require('../models/member')
const objectid = require('objectid')

// Get all labels of the group 
router.get('/', (req, res, next) => {
    if (!req.user_id) { return res.status(401).send('Not authenticated') }
	// let userId = req.user_id
	let groupId = req.body.group_id
	Member.find({group_id : groupId}).then((m) => {
		// console.log(m.group_id)
		if(m){
			Label.find({group_id : groupId}).exec().then((l) => {
				res.status(200).send(l)
			}).catch((error) => {
				return res.status(400).send('Impossibile to retrieve label')
			})
		}
		else{
			console.log("It is not a member of any group".red);
		}
	})
})

// Create a new label
router.post('/', (req, res, next) => {
	let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }
    
	let labelName = req.body.name.toString()
	if(!labelName)
		return res.status(400).send('Bad Request')
	
	let groupId = req.body.group_id
	if(!groupId)
		return res.status(400).send('Bad Request')
	console.log(userId);
	Group.findOne({group_id : groupId, owner_id : userId}).exec().then((g) => {
		console.log(g);
		if(g){
			Label.findOne({name : labelName}).exec().then((l) => {
				if(!l){
					const { 
						name, group_id
					} = req.body
		
					const newLabel = {
						name, 
						group_id
					}
					newLabel.label_id = objectid()
					try{
						Label.create(newLabel)
						res.status(200).send('Label created')
					}
					catch (error) {
						next(error)
					}
				}
				else{
					return res.status(400).send('Label already exists')
				}
			}).catch((error) => {
				console.log(error)
				return res.status(400).send('Impossible to retrieve label: ' + error)
			})
		}
		else{
			return res.status(400).send('Permissione Denied')
		}
	})
})

// TODO delete a label
router.delete('/', (req, res, next) => {
	
})

module.exports = router