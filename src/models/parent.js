const mongoose = require('mongoose')

const parentSchema = new mongoose.Schema({
  parent_id: {
    type: String,
    required: true
  },
  child_id: {
    type: String,
    required: true
  }
}, { timestamps: true })

mongoose.pluralize(null)
const model = mongoose.model('Parent', parentSchema)

parentSchema.virtual('child', {
  ref: 'Child',
  localField: 'child_id',
  foreignField: 'child_id',
  justOne: true
})



parentSchema.index({ parent_id: 1, child_id: 1 }, { unique: true })


module.exports = model
