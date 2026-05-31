import 'pinia';
import { PersistOptions } from 'pinia-plugin-persistedstate';

declare module 'pinia' {
  export interface DefineStoreOptions<
    Id extends string,
    S extends StateTree,
    G,
    A,
  > {
    /**
     * Persist store state.
     * @see https://github.com/prazdevs/pinia-plugin-persistedstate
     */
    persist?: boolean | PersistOptions;
  }
}
