<template>
  <div class="account-body">
      <accountEditor/>
  </div>
</template>

<script setup>
import accountEditor from './accountEditor.vue';


</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.account-body{
  position: relative;
  flex: 1 1 832px;
  min-width: 0;
  max-width: 100%;
  width: 100%;
  border: 1px solid #e1e2e5;
  border-left: 0;
  min-height: 500px;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 0 5px 5px 0;
  box-sizing: border-box;
  overflow: hidden;
}

@media (max-width: 900px){
  .account-body{
    flex-basis: 100%;
    width: 100%;
    max-width: none;
    min-height: 500px;
    border-left: 1px solid #e1e2e5;
    border-radius: 5px;
  }
}

</style>