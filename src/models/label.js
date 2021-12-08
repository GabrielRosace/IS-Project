const mongoose = require('mongoose')

const labelSchema = new mongoose.Schema(
{
    label_id: {
        type: String,
        required: true
    },
    name: {
        type : String,
        // unique : true,
        required : true
    },
    group_id: {
        type : String,
        // unique: true,
        required : true
    }
  }
)

const model = mongoose.model('Label', labelSchema)

module.exports = model
