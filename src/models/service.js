const mongoose = require('mongoose')

// model for the service that provides our app
const serviceSchema = new mongoose.Schema({
  service_id: {
    type: String,
    unique: true,
    required: true
  },
  owner_id: {
    type: String,
    required: true
  },
  group_id: {
    type: String,
    required: true
  },
  name: {
    type: String,
    required: true
  },
  description: String,
  location: {
    type: String,
    required: true
  },
  pattern: {
    type: String,
    required: true
  },
  car_space: String,
  lend_obj: String,
  lend_time: Date,
  pickuplocation: String,
  img: {
    type: String,
    required: true
  },
  recurrence: {
    type: Boolean
  }

}, { timestamps: true, toJSON: { virtuals: true } })

serviceSchema.index({ group_id: 1, createdAt: -1 })

mongoose.pluralize(null)
const model = mongoose.model('Service', serviceSchema)

module.exports = model
