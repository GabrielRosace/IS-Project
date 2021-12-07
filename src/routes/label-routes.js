const express = require('express')
const router = new express.Router()

const Label = require('../models/label')

// Get all labels
router.get('/', (req, res, next) => {
    if (!req.user_id) { return res.status(401).send('Not authenticated') }

	Label.find({}).exec().then((l) => {
        res.status(200).send(l)
	}).catch((error) => {
		return res.status(400).send('Impossibile to retrieve label')
	})
})

// Create a new label
router.post('/', (req, res, next) => {
    if (!req.user_id) { return res.status(401).send('Not authenticated') }
    let labelName = req.body.label
	if(!labelName)
		return res.status(400).send('Bad Request')
	Label.findOne({labelName}).exec().then((l) => {
		if(!l){
			const newLabel = { name }
            newLabel.name = labelName
		}
		else{
			return res.status(400).send('Label already exists')
		}
		try{
			Label.create(newLabel)
			res.status(200).send('Label created')
		}
		catch (error) {
    next(error)
  	}
	}).catch((error) => {
		return res.status(400).send('Impossibile to retrieve label')
	})
})

// TODO delete a label