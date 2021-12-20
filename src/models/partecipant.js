const mongoose = require('mongoose')

const partecipantSchema = new mongoose.Schema({
  // The same of user_id
  partecipant_id: {
    type: String,
    required: true
  },
  activity_id: {
    type: String,
    required: true
  },
  days: {
    type: [Date],
    required: true
  },
  service: {
    type: Boolean,
    required: true
  }
})

partecipantSchema.index({ partecipant_id: 1, activity_id: 1 }, { unique: true })

const model = mongoose.model('Partecipant', partecipantSchema)
module.exports = model
