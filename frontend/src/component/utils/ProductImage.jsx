import { useEffect, useState } from "react";
import { fetchProductImage } from "../services/imageService";

const ProductImage = ({ imageId }) => {
  const [imageSrc, setImageSrc] = useState(null);

  useEffect(() => {
    if (!imageId) return undefined;

    let objectUrl;
    let cancelled = false;

    fetchProductImage(imageId)
      .then((url) => {
        objectUrl = url;
        if (cancelled) {
          URL.revokeObjectURL(url);
        } else {
          setImageSrc(url);
        }
      })
      .catch((error) => {
        console.error(`Failed to load image ${imageId}:`, error);
      });

    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [imageId]);

  if (!imageSrc) return null;

  return (
    <div>
      <img src={imageSrc} alt='Product' />
    </div>
  );
};

export default ProductImage;
