import {
  configureStore,
  createListenerMiddleware,
  isRejected,
} from "@reduxjs/toolkit";
import { toast } from "react-toastify";
import searchReducer from "../store/features/searchSlice";
import categoryReducer from "../store/features/categorySlice";
import productReducer from "../store/features/productSlice";
import paginationReducer from "../store/features/paginationSlice";

// A rejected thunk used to be written to state.errorMessage and then never read
// by anything, so failures were invisible. Surfacing them here keeps the side
// effect out of the reducers while still covering every slice at once.
const errorListener = createListenerMiddleware();

errorListener.startListening({
  matcher: isRejected,
  effect: (action) => {
    toast.error(action.error?.message ?? "Something went wrong. Please try again.");
  },
});

export const store = configureStore({
  reducer: {
    search: searchReducer,
    category: categoryReducer,
    product: productReducer,
    pagination: paginationReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().prepend(errorListener.middleware),
});
