const express = require('express')
const router = new express.Router()

const Child = require('../models/child')
const Label = require('../models/label')
const Profile = require('../models/profile')
const Parent = require('../models/parent')

router.get('/', async (req, res, next) => {
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
  // Child.find({ child_id: { $in: ids } })
  // .select('given_name family_name image_id child_id birthdate suspended gender allergies other_info special_needs labels')
  // .lean()
  // .populate('image')
  // .populate('parent')
  // .exec()
  // .then((profiles) => {
  //   if (profiles.length === 0) {
  //   return res.status(404).send('Childen not found')
  //   }

  //   profiles.forEach(async (value,index) => {
  //     value.parent = value.parent.parent_id

  //     const parentprofile = await Profile.findOne({ user_id: value.parent }, 'user_id given_name family_name image_id').lean().populate('image')

  //     value.parent = parentprofile
  //     console.log(`${index}`)
  //     if (index == profiles.length-1) {
  //       res.json(profiles)
  //     }
  //   })
  // }).catch(next)

  Child.find({child_id : { $in : ids}}).lean().populate('image').populate('parent').then((c) => {
    for( let i = 0 ; i < c.length ; i++){
      Profile.findOne({user_id : c[i].parent.parent_id}).lean().populate('image').then((p) => {
        if(c[i].labels){
          for( let j = 0; j < c[i].labels.length; j++){
            Label.findOne({label_id : c[i].labels[j]}).then((l) => {
              c[i].labels[j] = l
            })
          }
        }
      })
    }
    return res.json(c)
  })


  // const profiles = await Child.find({ child_id: { $in: ids } }).lean().populate('image').populate('parent').exec()

  // console.log(profiles);
  
  // if (profiles.length === 0) {
  //   return res.status(404).send('Children not found')
  // }

  // for (let i = 0; i < profiles.length; i++){
  //   profiles[i].parent = await Profile.findOne({ user_id: profiles[i].parent.parent_id }, 'user_id given_name family_name image_id').lean().populate('image')
  //   if (profiles[i].labels) {
  //     for (let j = 0; j < profiles[i].labels.length; j++){
  //       profiles[i].labels[j] = await Label.findOne({label_id : profiles[i].labels[j]})
  //     }
  //   }
  // }
  // return res.json(profiles)
})



module.exports = router
