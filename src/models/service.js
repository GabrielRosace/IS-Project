const mongoose = require('mongoose')

const participantSchema = new mongoose.Schema({
  user_id: {
    type: String,
    required: true
  }
})
const ruleSchema = new mongoose.Schema({
  rule: {
    type: String,
    required: true
  }
})
// model for the service that provides our app
const serviceSchema = new mongoose.Schema({
  servizio_id: {
    type: String,
    unique: true,
    required: true
  },
  group_id: {
    type: String,
    required: true
  },
  group_name: {
    type: String,
    required: true
  },
  description: String,
  name: {
    type: String,
    required: true
  },
  // userid
  creator_id: {
    type: String,
    required: true
  },
  image_id: {
    type: String,
    required: true
  },
  partecipants: {
    type: [participantSchema],
    require: true
  },
  rules: {
    type: [ruleSchema],
    require: true
  },
  tipo: {
    type: String,
    required: true
  },
  repetition: {
    type: Boolean,
    required: true
  },
  repetition_type: {
    type: String
  },
  status: {
    type: String,
    required: true
  }
}, { timestamps: true, toJSON: { virtuals: true } })

serviceSchema.index({ group_id: 1, createdAt: -1 })

mongoose.pluralize(null)
const model = mongoose.model('Service', serviceSchema)

module.exports = model
