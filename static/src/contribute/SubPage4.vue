<template>
  <div v-if="store.userInformation.userName==='MoJi'" class="main-content">
    <div class="white">
        <div
        style="
          width: 1200px;
          height: 3px;
          background-color: #f8f8f8;
          transform: translate(0px, 60px);
        "
      ></div>
      <div
        style="
          color: #00a1d6;
          font-weight: 800;
          transform: translate(50px, 22px);
          cursor: pointer;
        "
      >
        用户管理
      </div>
      <div
        style="
          width: 65px;
          height: 3px;
          background-color: #00a1d6;
          transform: translate(50px, 35px);
        "
      ></div>
      <div
        style="
          position: absolute;
          z-index: 10;
          background-color: #f8f8f8;
          transform: translate(1060px, -50px);
          width: 200px;
          height: 1800px;
        "
      ></div>
      <el-input
        @keypress.enter="getUsersF"
        v-model="userName"
        style="
          width: 230px;
          height: 32px;
          font-size: 12px;
          transform: translate(790px, -7px);
        "
        placeholder="搜索用户"
      />
      <img
        @click="getUsersF"
        src="../img/搜索稿件.png"
        style="transform: translate(768px, -3px); width: 14px;cursor: pointer;"
      />
      <div
        style="
          display: flex;
          justify-content: space-around;
          width: 256px;
          font-size: 14px;
          color: #8c9195;
          transform: translate(42px, 28px);
        "
      >
        <span @click="type=-1" style="cursor: pointer"
         :style="{color:type===-1?'#00a1d6':'#8c9195'}"
          >全部用户</span
        >
        <span style="color: #e1e1e1">|</span>
        <span @click="type=0" style="cursor: pointer;"
        :style="{color:type===0?'#00a1d6':'#8c9195'}"
          >普通用户</span
        >
        <span style="color: #e1e1e1">|</span>
        <span @click="type=1" style="cursor: pointer"
        :style="{color:type===1?'#00a1d6':'#8c9195'}"
          >管理员</span
        >
      </div>
    <el-table :data="users" style="width: 70%;margin-top: 80px;margin-left: 110px;">
        <el-table-column prop="userName" label="用户名">
            <template #default="scope">
                <a class="tabel-user-name" :href="'../home?userId='+scope.row.id+'&homeMenu=1'" target="_blank"><span>{{ scope.row.userName }}</span></a>
            </template>
        </el-table-column>
        <el-table-column prop="头像" label="头像">
            <template #default="scope">
                <a :href="'../home?userId='+scope.row.id+'&homeMenu=1'" target="_blank"><img :src="scope.row.avatarAddress" style="width: 50px;height: 50px;border-radius: 50%;" /></a>
            </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" align="center">
            <template #default="scope">
                <span v-if="!scope.row.adminFlag">普通用户</span>
                <span v-else>管理员</span>
            </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
            <el-button size="small" @click="admin=scope.row,dialogVisible1=true">{{ scope.row.adminFlag?'取消管理员':'设为管理员' }}</el-button>
            </template>
        </el-table-column>
    </el-table>
    <div
        style="
          display: flex;
          justify-content: right;
          width: 950px;
          margin-top: 30px;
        "
      >
        <el-pagination
          :current-page="pageNum"
          :page-size="10"
          :total="total"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          :hide-on-single-page="true"
        />
    </div>
    <el-dialog
      v-model="dialogVisible1"
      append-to-body
      width="392"
      center
      :modal="false"
      style="transform: translate(0px, 100px); height: 200px"
    >
      <span
        style="
          transform: translate(80px, 0px);
          position: absolute;
          font-size: 18px;
          font-weight: 800;
          color: #8c9195;
        "
      >
        {{ !admin.adminFlag? `确定将 ${admin.userName} 设为管理员吗`:`确定将 ${admin.userName} 取消管理员吗` }}
      </span>
      <template #footer>
        <div class="dialog-footer" style="transform: translate(-10px, 50px)">
          <el-button class="yesDelete" @click="putAdminF">确定</el-button>
          <el-button
            type="primary"
            @click="dialogVisible1=false"
            style="
              background-color: white;
              color: black;
              border: 1px solid #b3b3b3;
              height: 28px;
            "
            >取消</el-button
          >
        </div>
      </template>
    </el-dialog>
  </div>
</div>
  <div v-else>
    <div style="display: flex; justify-content: center; align-items: center">
      <h1>您没有访问权限</h1>
    </div>
  </div>
</template>

<script>
import {useGlobalStore} from "../store/store";
import { getUsers,putAdmin } from "../api/user/index";
import { onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from "element-plus";
export default {
    name: "SubPage4",
    setup(){

    const users=reactive([]);
    const store=useGlobalStore();
    const userName=ref("");
    const type=ref(-1);
    const pageNum=ref(1);
    const total=ref(0);
    const admin=ref({});
    const dialogVisible1=ref(false);


    onMounted(()=>{
        getUsersF();
    })

    //查询用户
    function getUsersF(){
        getUsers(store.token,store.userId,pageNum.value,userName.value,type.value).then(res=>{
            users.length=0;
            Object.assign(users,res.data.data.records);
            total.value=res.data.data.total;
        })
    }

    //设为管理员
    function putAdminF(){
        
        putAdmin(store.token,store.userId,admin.value.id).then(res=>{
            if(res.data.code===1){
                ElMessage({
                message: !admin.value.adminFlag?`已将 ${admin.value.userName} 设为管理员`:`已将 ${admin.value.userName} 取消管理员`,
                type: "info",
                plain: true,
                duration: 1700,
                });
                users.forEach(item=>{
                    if(item.id===admin.value.id){
                        item.adminFlag=!item.adminFlag;
                    }
                })
                admin.value={};
                dialogVisible1.value=false;
            }else{
                ElMessage({
                message: res.data.msg,
                type: "info",
                plain: true,
                duration: 1700,
                });
            }
        })
    }

    const handleCurrentChange = (page) => {
      pageNum.value = page;
      getUsersF();
      window.scrollTo({top: 0, behavior: "smooth"});
    };

    watch(type,()=>{
        getUsersF();
    })

    return{
        store,
        userName,
        getUsersF,
        type,
        users,
        pageNum,
        total,
        handleCurrentChange,
        admin,
        dialogVisible1,
        putAdminF,
    }


    }

}
</script>

<style>

.main-content {
  background-color: #f8f8f8;
  border-radius: 5px;
  transform: translate(-100px, -20px);
  overflow-y: hidden;
  overflow-x: hidden;
  height: 1000px;
  width: 1500px;
}
.white {
  transform: translate(120px, 20px);
  width: 1200px;
  height: 2200px !important;
  z-index: 10;
  background-color: white;
}

.tabel-user-name{
 color: black;
 transition: color 0.3s ease;
}

.tabel-user-name:hover{
color: #00a1d6;
}

</style>