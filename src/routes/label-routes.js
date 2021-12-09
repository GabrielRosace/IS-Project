const express = require('express')
const router = new express.Router()

const Label = require('../models/label')
const Group = require('../models/group')
const Member = require('../models/member')
const Child = require('../models/child')
const Parent = require('../models/parent')
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
	// console.log(userId);
	Group.findOne({group_id : groupId, owner_id : userId}).exec().then((g) => {
		// console.log(g);
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
		if(!l) console.log("Label does not exist");
		Group.findOne({group_id : l.group_id}).then((g) => {
			if(!g) console.log("Group does not exists");
			Member.find({group_id : g.group_id}).then( async (m) => {
				if(!m) console.log("Member does not exist");
				for(let i = 0; i < m.length; i++){
					let parents = await Parent.find({parent_id : m[i].user_id})
					if(parents){
						for(let j = 0; j < parents.length; j++){
							Child.find({child_id : parents[j].child_id}).then((children) => {
								if(children){
									for(let k = 0; k < children.length; k++){
										for(let t = 0; t < children[k].labels.length; t++){
											if(l.label_id == children[k].labels[t]){
												let childLabels = children[k].labels
												// delete children[k].labels[t]
												delete childLabels[t]
												children[k].labels = childLabels
												console.log("Deleted");
											}
										}
										children[k].save().then().catch((error) => {
											console.log(error);
										})
									}
								}
							})
						}
					}
				}
			})
		})
	})


	await Label.deleteOne({label_id : label_id})
	return res.status(200).send('Label deleted')
})

// TODO controllare se è bambino
// Add a label to a child
router.post('/child', (req, res, next) => {
	let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }

	let childId = req.body.child_id
	if(!childId) { return res.status(400).send('Bad Request') }

	let labelId = req.body.label_id
	if(!labelId) { return res.status(400).send('Bad Request') }

	// let labelName = req.body.label_name
	// if(!labelName) { return res.status(400).send('Bad Request') }

	// let labelGroup = req.body.label_group
	// if(!labelGroup) { return res.status(400).send('Bad Request') }

	// Label.findOne({name : labelName, group_id : labelGroup}).then((l) => {
	Label.findOne({label_id : labelId}).then((l) => {
		if(l){
			Child.findOne({child_id : childId}).then((c) => {
				if(c){
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
router.get('/child/:child_id', (req, res, next) => {
	let userId = req.user_id
    if (!userId) { return res.status(401).send('Not authenticated') }

	let childId = req.params.child_id
	if(!childId) { return res.status(400).send('Bad Request') }

	Child.findOne({child_id : childId}).then((c) => {
		if(c){
			let childLabels = []
			c.labels.forEach((label) => {
				console.log(label);
				Label.findOne({label_id : label}).then((l) => {
					console.log(l);
					childLabels.push(l)
				})
				
			})
			console.log('Labels:' + childLabels);
			return res.status(200).send(childLabels)
		}
		else{
			return res.status(400).send('Child does not exist')
		}
	})
})

module.exports = router