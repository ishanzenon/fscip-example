import { configureStore } from '@reduxjs/toolkit';
import { authSlice } from './slices/authSlice';
import { accountSlice } from './slices/accountSlice';

export const store = configureStore({
  reducer: {
    auth: authSlice.reducer,
    account: accountSlice.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;