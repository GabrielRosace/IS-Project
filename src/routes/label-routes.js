const express = require('express')
const router = new express.Router()

const Label = require('../models/label')
const Group = require('../models/group')
const Member = require('../models/member')
const Child = require('../models/child')
const Parent = require('../models/parent')
const Activity = require('../models/activity')
const objectid = require('objectid')

// Get all labels of the group 
router.get('/:group_id', (req, res, next) => {
    if (!req.user_id) { return res.status(401).send('Not authenticated') }
	let groupId = req.params.group_id
	Member.find({group_id : groupId}).then((m) => {
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
	
	Group.findOne({group_id : groupId}).then((g) => {
		if(g){
			if(g.owner_id != userId)
			return res.status(400).send('Unauthorized')
		}
		else{
			return res.status(400).send('Group does not exist')
		}
	})

	Group.findOne({group_id : groupId, owner_id : userId}).exec().then((g) => {
		// console.log(g);
		if(g){
			Label.findOne({name : labelName, group_id : g.group_id}).exec().then((l) => {
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

// delete a label
router.delete('/:label_id', async (req, res, next) => {
	let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }

	let label_id = req.params.label_id
	if(!label_id) return res.status(400).send('Bad Request')

	Label.findOne({label_id : label_id}).then((l) => {
		Group.findOne({group_id : l.group_id}).then((g) => {
			if(g.owner_id != userId)
				return res.status(400).send('Unauthorized')
		})
	})

	Label.findOne({label_id : label_id}).then((l) => {
		if(!l) return res.status(400).send('Label does not exist')
		Group.findOne({group_id : l.group_id}).then((g) => {
			if(!g) return res.status(400).send('Group does not exist')
			Member.find({group_id : g.group_id}).then( async (m) => {
				if(!m) return res.status(400).send('Empty group')
				for(let i = 0; i < m.length; i++){
					let parents = await Parent.find({parent_id : m[i].user_id})
					if(parents){
						for(let j = 0; j < parents.length; j++){
							Child.updateOne({child_id : parents[j].child_id}, {$pull : { labels : { $in : [l.label_id]}}}).then((data) => {
								console.log("Label deleted");
							})
						}
					}
					else{
						return res.status(400).send('No child')
					}
				}
			})
			Activity.updateMany({group_id : g.group_id}, { $pull : {labels : { $in : [l.label_id]}}}).then((data) => {
				console.log('Label deleted from activities');
			})
		})
	})


	await Label.deleteOne({label_id : label_id})
	return res.status(200).send('Label deleted')
})

// Add a label to a child
router.post('/child', (req, res, next) => {
	let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }

	let childId = req.body.child_id
	if(!childId) { return res.status(400).send('Bad Request') }

	let labelId = req.body.label_id
	if(!labelId) { return res.status(400).send('Bad Request') }

	Label.findOne({label_id : labelId}).then((l) => {
		if(l){
			Child.findOne({child_id : childId}).then((c) => {
				if(c){
					Parent.findOne({parent_id : userId, child_id : c.child_id}).then((p) => {
						if(!p) return res.status(400).send('Child does not belong to the user')
					})
					c.labels.push(l.label_id)
					c.save().then(() => {
						return res.status(200).send('Label inserted')
					}).catch((error) => {
						console.log('Save error: ' + error);
					})
				}
				else{
					return res.status(400).send('Child does not exist')
				}
			})
		}else{
			// ! Non esiste l'etichetta
			return res.status(400).send('Label does not exists')
		}
	})
})

// Get all labels of a child
router.get('/child/:child_id', async (req, res, next) => {
	let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }

	let childId = req.params.child_id
	if(!childId) { return res.status(400).send('Bad Request') }

	Child.findOne({child_id : childId}).then( async (c) => {

		Parent.findOne({parent_id : userId, child_id : c.child_id}).then((p) => {
			if(!p) return res.status(400).send('Child does not belong to the user')
		})

		if(c){
			let childLabels = []
			for(let i = 0 ; i < c.labels.length ; i++){
				let label = await Label.findOne({label_id : c.labels[i]})
				childLabels.push(label)
			}
			return res.status(200).send(childLabels)
		}
		else{
			// ! Child does not exist
			return res.status(400).send('Child does not exist')
		}
	})
})

// Delete a label of a child
router.delete('/child/:label_id/:child_id', (req, res, next) => {
	let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }

	let label_id = req.params.label_id
	if(!label_id) return res.status(400).send('Bad Request')

	let child_id = req.params.child_id
	if(!child_id) return res.status(400).send('Bad Request')

	Label.findOne({label_id : label_id}).then((l) => {
		if(!l) return res.status(400).send('Label does not exists')

		Parent.findOne({parent_id : userId, child_id : child_id}).then((p) => {
			if(!p) return res.status(400).send('Child does not belong to the user')
		})

		Child.updateOne({child_id : child_id}, {$pull : {labels : {$in : [l.label_id]}}}).then((data) => {
			console.log('Label deleted');
		})
		return res.status(200).send('Label deleted')
	})
})

module.exports = router