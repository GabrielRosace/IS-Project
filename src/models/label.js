const mongoose = require('mongoose')

const labelSchema = new mongoose.Schema(
{
    name : {
        type : String,
        unique : true,
        required : true
    }
}
)

export function getSchema() { return labelSchema }

const model = mongoose.model('Label', labelSchema)

module.exports = model