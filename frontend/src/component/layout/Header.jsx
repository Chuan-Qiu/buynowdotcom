import { FaShoppingCart } from "react-icons/fa";
import { Link } from "react-router-dom";

const Header = () => {
  return (
    <div className="d-flex justify-content-end p-2 px-4 bg-dark text-white">
      <Link to="#" className="text-white text-decoration-none d-flex align-items-center gap-2">
        <FaShoppingCart className="shopping-cart-icon" />
        <span>Cart</span>
      </Link>
    </div>
  );
};

export default Header;
