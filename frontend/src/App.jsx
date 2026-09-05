import {
  Route,
  RouterProvider,
  createBrowserRouter,
  createRoutesFromElements,
} from "react-router-dom";
import RootLayout from "./component/layout/RootLayout";
import Home from "./component/home/Home";
import Products from "./component/product/Products";
import ProductDetails from "./component/product/ProductDetails";

function App() {
  const router = createBrowserRouter(
    createRoutesFromElements(
      <Route path='/' element={<RootLayout />}>
        <Route index element={<Home />} />
        <Route path='/products' element={<Products />} />
        <Route path='/products/:name' element={<Products />} />
        <Route
          path='/products/category/:categoryId/products/'
          element={<Products />}
        />
        <Route
          path='/product/:productId/details'
          element={<ProductDetails />}
        />
      </Route>
    )
  );

  return <RouterProvider router={router} />;
}

export default App;
