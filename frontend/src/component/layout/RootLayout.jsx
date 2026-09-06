import { Outlet } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import NavBar from "../layout/NavBar";
import Footer from "../layout/Footer";
import Header from "../layout/Header";

const RootLayout = () => {
  return (
    <main>
      <Header />
      <NavBar />
      <div>
        <Outlet />
      </div>
      <Footer />
      {/* Mounted once at the shell so errors surface on every route, not just Home. */}
      <ToastContainer position='top-right' autoClose={5000} />
    </main>
  );
};

export default RootLayout;
