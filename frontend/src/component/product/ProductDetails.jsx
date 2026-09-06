import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { getProductById } from "../../store/features/productSlice";
import { useSelector, useDispatch } from "react-redux";
import ImageZoomify from "../common/ImageZoomify";
import QuantityUpdater from "../utils/QuantityUpdater";
import { FaShoppingCart } from "react-icons/fa";

const ProductDetails = () => {
  const { productId } = useParams();
  const dispatch = useDispatch();
  const product = useSelector((state) => state.product.product);

  useEffect(() => {
    dispatch(getProductById(productId));
  }, [dispatch, productId]);

  return (
    <div className='container'>
      {product ? (
        <div className='row product-details'>
          <div className='col-md-2'>
            {product.images.map((img) => (
              <div key={img.id} className='image-container'>
                <ImageZoomify imageId={img.id} />
              </div>
            ))}
          </div>
          <div className='col-md-8 details-container'>
            <h1 className='product-name'>{product.name}</h1>
            <h4 className='price'>${product.price}</h4>
            <p className='product-description'>{product.description}</p>
            <p className='product-name'>Brand: {product.brand}</p>
            <p className='product-name'>
              Rating: <span className='rating'>some stars</span>
            </p>
            <p>
              {product.inventory > 0 ? (
                <span className='text-success'>{product.inventory} in stock</span>
              ) : (
                <span className='text-danger'>Out of stock</span>
              )}
            </p>
            <p>Quantity:</p>
            <QuantityUpdater />
            <div className='d-flex gap-2 mt-3'>
              <button className='add-to-cart-button'>
                <FaShoppingCart /> Add to cart
              </button>
              <button className='buy-now-button'>Buy now</button>
            </div>
          </div>
        </div>
      ) : (
        <p>No product</p>
      )}
    </div>
  );
};

export default ProductDetails;
