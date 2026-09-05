# BuyNow — Frontend

React 19 single-page app for the BuyNow storefront. Talks to the [backend](../backend) REST API.

## Stack

React 19 · Redux Toolkit 2 · React Router 7 · Vite 8 · Axios · React-Bootstrap · react-slick · react-toastify

## Run

```bash
npm install
npm run dev      # http://localhost:5174
npm run build
npm run lint
```

The backend must be running on `http://localhost:9090` — see the [root README](../README.md).

## Structure

```
src/
├── component/
│   ├── layout/      Header · NavBar · Footer · RootLayout
│   ├── home/        Home
│   ├── hero/        Hero · HeroSlider
│   ├── product/     Products · ProductDetails · ProductCard
│   ├── search/      SearchBar
│   ├── common/      Paginator · SideBar · LoadSpinner · ImageZoomify · NoProductsAvailable
│   ├── utils/       ProductImage · QuantityUpdater
│   └── services/    api.js (single Axios instance) · ProductService.js
└── store/
    ├── store.js
    └── features/    productSlice · searchSlice · paginationSlice · categorySlice
```

## Design Notes

**Components are split by role, not just by feature.** Page-level components (`Products.jsx`)
own orchestration — they read from the store, filter, and pass a slice of data down.
Presentational components (`ProductCard.jsx`) take plain props and render; they know nothing
about Redux, routing, or the API, so they stay reusable in any context.

**One slice per domain, not one global object.** `SearchBar`, `SideBar`, and `Paginator` are
siblings with no parent/child path between them, yet all three affect the same product list —
that is the reason for Redux here. Each owns its own slice; `Products.jsx` is the only place
that combines them into one filtered, paginated result. Adding a new filter means adding a
slice and one condition, not surgery on the existing ones.

**One async pattern everywhere.** Every API-driven slice uses `createAsyncThunk` and handles
`pending / fulfilled / rejected` in `extraReducers`, so new endpoints wire into the UI
predictably.

**A single network seam.** No component calls `fetch`/`axios` directly — everything goes
through the one Axios instance in `component/services/api.js`. If the base URL changes or all
requests need an auth header, there is exactly one file to touch.

See [DESIGN.md](../DESIGN.md) for the full architecture document.
