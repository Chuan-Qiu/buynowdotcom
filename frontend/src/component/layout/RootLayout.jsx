import React from 'react'
import {Outlet} from "react-router-dom"
import NavBar from '../layout/NavBar';
import Footer from '../layout/Footer';
import Header from '../layout/Header';

const RootLayout = () => {
  return (
    <main>
      <Header />
      <NavBar/>
      <div>
        <Outlet />
      </div>
      <Footer />
    </main>
  );
}

export default RootLayout
