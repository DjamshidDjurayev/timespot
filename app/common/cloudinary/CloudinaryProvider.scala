package common.cloudinary

import com.cloudinary.Cloudinary

trait CloudinaryProvider {
  def getClient: Cloudinary
}
