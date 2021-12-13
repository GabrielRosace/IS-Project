const express = require('express')
const router = new express.Router()

const Child = require('../models/child')
const Label = require('../models/label')
const Profile = require('../models/profile')
const Parent = require('../models/parent')

// Ritorna tutte le informazioni riguardanti il bambino specificato.
// In particolare viene inviato al client un JSON con tutti i dati del bambino compresa l'immagine, le etichette associate e le informazioni di un genitore, compresa anche l'immagine 
// di quest'ultimo.
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


  //* Working query but too slow
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
  

  // await Child.find({child_id : { $in : ids}}).lean().populate('image').populate('parent').then( async (c) => {
  //   if(c.length === 0) return res.status(400).send('Children non found')
    
  //   for( let i = 0 ; i < c.length ; i++){
  //     await Profile.findOne({user_id : c[i].parent.parent_id}).lean().populate('image').then( async (p) => {
  //       if(c[i].labels){
  //         for( let j = 0; j < c[i].labels.length; j++){
  //           await Label.findOne({label_id : c[i].labels[j]}).then((l) => {
  //             console.log(l);
  //             c[i].labels[j] = l
  //           })
  //         }
  //       }
  //     })
  //   }
  //   console.log("return");
  //   return res.json(c)
  // })


  //* Working query a little bit faster
  // const profiles = await Child.find({ child_id: { $in: ids } }).lean().populate('image').populate('parent').exec()

  // // console.log(profiles);
  
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
  
  //* The final query that uses aggregate is faster than the others
  const profiles = await Child.aggregate([
    {
      '$lookup': {
        'from': 'Label', 
        'localField': 'labels', 
        'foreignField': 'label_id', 
        'as': 'labels'
      }
    }, {
      '$lookup': {
        'from': 'Image', 
        'localField': 'image_id', 
        'foreignField': 'image_id', 
        'as': 'image'
      }
    }, {
      '$lookup': {
        'from': 'Parent', 
        'localField': 'child_id', 
        'foreignField': 'child_id', 
        'as': 'parent'
      }
    }, {
      '$lookup': {
        'from': 'Profile', 
        'localField': 'parent.parent_id', 
        'foreignField': 'user_id', 
        'as': 'parent'
      }
    }, {
      '$project': {
        'labels': 1, 
        'birthdate': 1, 
        'given_name': 1, 
        'family_name': 1, 
        'gender': 1, 
        'allergies': 1, 
        'other_info': 1, 
        'special_needs': 1, 
        'background': 1, 
        'suspended': 1, 
        'child_id': 1, 
        'image_id': 1, 
        'createdAt': 1, 
        'updatedAt': 1, 
        'parent': {
          '$arrayElemAt': [
            '$parent', 0
          ]
        }, 
        'image': {
          '$arrayElemAt': [
            '$image', 0
          ]
        }
      }
    }, {
      '$lookup': {
        'from': 'Image', 
        'localField': 'parent.image_id', 
        'foreignField': 'image_id', 
        'as': 'parent.image'
      }
    }, {
      '$project': {
        'labels': 1, 
        'birthdate': 1, 
        'given_name': 1, 
        'family_name': 1, 
        'gender': 1, 
        'allergies': 1, 
        'other_info': 1, 
        'special_needs': 1, 
        'background': 1, 
        'suspended': 1, 
        'child_id': 1, 
        'image_id': 1, 
        'createdAt': 1, 
        'updatedAt': 1, 
        'parent': {
          '_id': 1, 
          'given_name': 1, 
          'family_name': 1, 
          'user_id': 1, 
          'image_id': 1, 
          'image': {
            '$arrayElemAt': [
              '$parent.image', 0
            ]
          }
        }, 
        'image': 1
      }
    }, {
    "$match": {
      child_id: {$in: ids}
    }
  }
  ])  
  return res.json(profiles)
})



module.exports = router
