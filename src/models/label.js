const mongoose = require('mongoose')

const labelSchema = new mongoose.Schema(
{
    label_id: {
        type: String,
        required: true
    },
    name: {
        type : String,
        unique : false,
        required : true
    },
    group_id: {
        type : String,
        unique: false,
        required : true
    }
  }
)

labelSchema.index({ name: 1, group_id: 1 }, { unique: true })

const model = mongoose.model('Label', labelSchema)

module.exports = model
