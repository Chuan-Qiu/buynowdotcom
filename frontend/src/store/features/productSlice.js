import { createSlice, createAsyncThunk, isPending, isRejected } from "@reduxjs/toolkit";
import { api } from "../../component/services/api";

export const getAllProducts = createAsyncThunk(
  "product/getAllProducts",
  async () => {
    const response = await api.get("/products/all");
    return response.data.data;
  }
);

export const getAllBrands = createAsyncThunk(
  "product/getAllBrands",
  async () => {
    const response = await api.get("/products/distinct/brands");
    return response.data.data;
  }
);

export const getDistinctProductsByName = createAsyncThunk(
  "product/getDistinctProductsByName",
  async () => {
    const response = await api.get("/products/distinct/products");
    return response.data.data;
  }
);

export const getProductById = createAsyncThunk(
  "product/getProductById",
  async (productId) => {
    const response = await api.get(`/products/product/${productId}/product`);
    return response.data.data;
  }
);

export const getProductsByCategory = createAsyncThunk(
  "product/getProductsByCategory",
  async (categoryId) => {
    const response = await api.get(`/products/category/${categoryId}/products`);
    return response.data.data;
  }
);

const PRODUCT_THUNKS = [
  getAllProducts,
  getAllBrands,
  getDistinctProductsByName,
  getProductById,
  getProductsByCategory,
];

const initialState = {
  products: [],
  product: null,
  distinctProducts: [],
  brands: [],
  selectedBrands: [],
  quantity: 1,
  errorMessage: null,
  isLoading: true,
};

const productSlice = createSlice({
  name: "product",
  initialState,
  reducers: {
    filterByBrands: (state, action) => {
      const { brand, isChecked } = action.payload;
      if (isChecked) {
        state.selectedBrands.push(brand);
      } else {
        state.selectedBrands = state.selectedBrands.filter((b) => b !== brand);
      }
    },
    decreaseQuantity: (state) => {
      if (state.quantity > 1) {
        state.quantity--;
      }
    },
    increaseQuantity: (state) => {
      state.quantity++;
    },
  },

  extraReducers: (builder) => {
    builder
      .addCase(getAllProducts.fulfilled, (state, action) => {
        state.products = action.payload;
        state.errorMessage = null;
        state.isLoading = false;
      })
      .addCase(getAllBrands.fulfilled, (state, action) => {
        state.brands = action.payload;
        state.isLoading = false;
      })
      .addCase(getDistinctProductsByName.fulfilled, (state, action) => {
        state.distinctProducts = action.payload;
        state.isLoading = false;
      })
      .addCase(getProductById.fulfilled, (state, action) => {
        state.product = action.payload;
        state.isLoading = false;
      })
      .addCase(getProductsByCategory.fulfilled, (state, action) => {
        state.products = action.payload;
        state.errorMessage = null;
        state.isLoading = false;
      })
      // Every thunk in this slice shares the same pending/rejected behaviour, so
      // it is declared once here rather than repeated per case. Without the
      // rejected branch, isLoading (which starts true) would never be cleared on
      // failure and the page would spin forever.
      .addMatcher(isPending(...PRODUCT_THUNKS), (state) => {
        state.isLoading = true;
        state.errorMessage = null;
      })
      .addMatcher(isRejected(...PRODUCT_THUNKS), (state, action) => {
        state.isLoading = false;
        state.errorMessage = action.error?.message ?? "Request failed";
      });
  },
});

export const { filterByBrands, decreaseQuantity, increaseQuantity } =
  productSlice.actions;
export default productSlice.reducer;
