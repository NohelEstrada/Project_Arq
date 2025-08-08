<script setup lang="ts">
const changes = useChanges();
</script>

<template>
  <div class="from-s-background to-sh-background h-full w-full bg-linear-65">
    <div v-for="(change, index) in changes">
      <div class="responsive-grid">
        <div class="card outline-error/30 mb-6 outline-4">
          <div v-for="(key, index) in Object.keys(change.oldValue)" class="label mb-4 flex">
            <div class="me-2 font-semibold">
              {{ change.fieldNames[index] }}
            </div>
            {{ change.oldValue[key] }}
          </div>
        </div>
        <svg class="text-primary mb-4 flex w-full justify-center" xmlns="http://www.w3.org/2000/svg" height="60px"
          viewBox="0 -960 960 960" width="40px" fill="currentColor">
          <path d="M440-800v487L216-537l-56 57 320 320 320-320-56-57-224 224v-487h-80Z" />
        </svg>
        <div class="card outline-success/30 mb-6 outline-4">
          <div v-for="(key, index) in Object.keys(change.newValue)" class="label mb-4 flex">
            <div class="me-2 font-semibold">
              {{ change.fieldNames[index] }}
            </div>
            {{ change.newValue[key] }}
          </div>
        </div>
      </div>
      <div class="flex justify-center mb-6">
        <button class="btn me-2 flex" @click="() => { applyChange(index); changes.splice(index, 1); }">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M840-680v480q0 33-23.5 56.5T760-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z" />
          </svg>Save
        </button>
        <button class="btn flex"
          @click="() => { changes.splice(index, 1); notify({ title: 'Changes discarded', description: 'Changes discarded', type: 'error' }); }">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="m336-280 144-144 144 144 56-56-144-144 144-144-56-56-144 144-144-144-56 56 144 144-144 144 56 56ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z" />
          </svg>Discard
        </button>
      </div>
    </div>

  </div>
</template>
