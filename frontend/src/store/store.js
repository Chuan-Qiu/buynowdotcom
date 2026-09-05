import { configureStore } from "@reduxjs/toolkit";
import searchReducer from "../store/features/searchSlice";
import categoryReducer from "../store/features/categorySlice";
import productReducer from "../store/features/productSlice";
import paginationReducer from "../store/features/paginationSlice";

export const store = configureStore({
  reducer: {
    search: searchReducer,
    category: categoryReducer,
    product: productReducer,
    pagination: paginationReducer,
  },
});
