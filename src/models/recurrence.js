const mongoose = require('mongoose')

// // TODO aggiungere flag per identificare se evento o servizio
// // TODO trasformare start_date e end_date in array
const recurrenceSchema = new mongoose.Schema({
  recurrence_id: {
    type: String,
    unique: true,
    required: true
  },
  activity_id: {
    type: String,
    unique: true,
    required: true
  },
  // true = servizio per la persona, false = evento ricorrente
  service: {
    type: Boolean,
    required: true
  },
  type: {
    type: String,
    required: true,
    enum: ['daily', 'weekly', 'monthly']
  },
  start_date: {
    type: [Date],
    required: true
  },
  end_date: {
    type: [Date],
    required: true
  }
})

const model = mongoose.model('Recurrence', recurrenceSchema)
module.exports = model
