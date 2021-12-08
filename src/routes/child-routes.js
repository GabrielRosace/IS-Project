const express = require('express')
const router = new express.Router()

const Child = require('../models/child')
const Label = require('../models/label')
const Profile = require('../models/profile')
const Parent = require('../models/parent')

router.get('/', (req, res, next) => {
  if (!req.user_id) { return res.status(401).send('Not authenticated') }
  const { ids } = req.query
  if (!ids) {
    return res.status(400).send('Bad Request')
  }

  //* Old function
  // Child.find({ child_id: { $in: ids } })
  //   .select('given_name family_name image_id child_id suspended')
  //   .populate('image')
  //   .lean()
  //   .exec()
  //   .then(profiles => {
  //     if (profiles.length === 0) {
  //       return res.status(404).send('Children not found')
  //     }
  //     Parent.findOne({ child_id: profiles})
  //     res.json(profiles)
  //   }).catch(next)


  //? Si può fare meglio
  Child.find({ child_id: { $in: ids } })
  .select('given_name family_name image_id child_id birthdate suspended gender allergies other_info special_needs')
  .lean()
  .populate('image')
  .populate('parent')
  .exec()
  .then((profiles) => {
    if (profiles.length === 0) {
    return res.status(404).send('Childen not found')
    }

    profiles.forEach(async (value,index) => {
      value.parent = value.parent.parent_id

      const parentprofile = await Profile.findOne({ user_id: value.parent }, 'user_id given_name family_name image_id').lean().populate('image')

      value.parent = parentprofile
      console.log(`${index}`)
      if (index == profiles.length-1) {
        res.json(profiles)
      }
    })
  }).catch(next)
})

// TODO: Insert a label
// router.post('/label/:childId', (req, res, next) => {
// 	if (!req.user_id) { return res.status(401).send('Not authenticated') }
// 	// const { ids } = req.query
// 	// if (!ids){
// 	// 	return res.status(400).send('Bad Request')
// 	// }

// 	let labelName = req.body.label
// 	if(!labelName)
// 		return res.status(400).send('Bad Request')
// 	Label.findOne({labelName}).exec().then((l) => {
// 		if(l){
// 			const newLabel = { name }
// 		}
// 		else{
// 			return res.status(400).send('Label does not exists')
// 		}
// 		try{
// 			Label.create(newLabel)
// 			res.status(200).send('Label created')
// 		}
// 		catch (error) {
//     next(error)
//   	}
// 	}).catch((error) => {
// 		return res.status(400).send('Impossibile to retrieve label')
// 	})
// })

// TODO: Get all child labels
// router.get

// TODO: Delete a label

module.exports = router
