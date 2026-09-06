import { useEffect, useState } from "react";
import ImageZoom from "react-medium-image-zoom";
import "react-medium-image-zoom/dist/styles.css";
import { fetchProductImage } from "../services/imageService";

const ImageZoomify = ({ imageId }) => {
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
    <ImageZoom>
      <img src={imageSrc} alt='Product' className='resized-image' />
    </ImageZoom>
  );
};

export default ImageZoomify;
