const express = require('express')
const router = new express.Router()


const Image = require('../models/image')

router.get('/:image_id', async (req, res, next) => {
  if (!req.user_id) { return res.status(401).send('Not authenticated') }

  let image_id = req.params.image_id

  const image = await Image.findOne({ image_id: image_id })
  
  if (image) {
    return res.status(200).json(image)
  } else {
    return res.status(404).send("Image not found")
  }
})

module.exports = router