package common.cloudinary

import com.cloudinary.Cloudinary
import com.google.inject.Singleton

@Singleton
class CloudinaryProviderImpl extends CloudinaryProvider {
  override def getClient: Cloudinary = {
    new Cloudinary(Map(
      "cloud_name" -> constants.CLOUDINARY_CLOUD_NAME,
      "api_key" -> constants.CLOUDINARY_API_KEY,
      "api_secret" -> constants.CLOUDINARY_API_SECRET
    ))
  }
}
