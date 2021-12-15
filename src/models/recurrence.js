const mongoose = require('mongoose')

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
    type: {
        type: String,
        required: true,
        enum: ['daily', 'weekly', 'monthly']
    },
    start_date: {
        type: Date,
        required: true
    },
    end_date: {
        type: Date,
        required: true
    }
})

const model = mongoose.model('Recurrence', recurrenceSchema)
module.exports = model