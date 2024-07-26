<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="formInline" class="demo-form-inline">
        <el-form-item label="主机">
          <el-input v-model="formInline.host" placeholder="主机"></el-input>
        </el-form-item>
        <el-form-item label="端口号">
          <el-input v-model="formInline.port" placeholder="端口号" style="width: 100px;"></el-input>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="formInline.user" placeholder="用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="formInline.password" placeholder="密码"></el-input>
        </el-form-item>
        <el-form-item label="库号">
          <el-select v-model="formInline.database" placeholder="库号">
            <el-option label="db0" value="0"></el-option>
            <el-option label="db1" value="1"></el-option>
            <el-option label="db2" value="2"></el-option>
            <el-option label="db3" value="3"></el-option>
            <el-option label="db4" value="4"></el-option>
            <el-option label="db5" value="5"></el-option>
            <el-option label="db6" value="6"></el-option>
            <el-option label="db7" value="7"></el-option>
            <el-option label="db8" value="8"></el-option>
            <el-option label="db9" value="9"></el-option>
            <el-option label="db10" value="10"></el-option>
            <el-option label="db11" value="11"></el-option>
            <el-option label="db12" value="12"></el-option>
            <el-option label="db13" value="13"></el-option>
            <el-option label="db14" value="14"></el-option>
            <el-option label="db15" value="15"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList" v-permission="'redis:list'">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="plus" @click="exportData" v-permission="'redis:export'">导出</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table :data="list" v-loading="listLoading"  border fit
              highlight-current-row>
      <el-table-column align="center" label="序号" width="80">
        <template slot-scope="scope">
          <span v-text="getIndex(scope.$index)"> </span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="key" label="键" style="width: 60px;"></el-table-column>
      <el-table-column align="center" prop="value" label="值" style="width: 60px;"/>
      <el-table-column align="center" prop="type" label="类型" width="170"/>
      <el-table-column align="center" label="管理" width="200" >
        <template slot-scope="scope">
          <el-button type="primary" icon="edit" @click="showUpdate(scope.$index)"  v-permission="'redis:update'">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="listQuery.pageNum"
      :page-size="listQuery.pageRow"
      :total="totalCount"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper" v-if="showPagination">
    </el-pagination>
    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form class="small-space" :model="tempArticle" label-position="left" label-width="60px"
               style='width: 500px; margin-left:50px;'>
        <el-form-item label="文章">
          <el-input type="textarea" style="width:100%" show-word-limit v-model="tempArticle.content"  maxlength="100">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button v-if="dialogStatus==='create'" type="success" @click="createArticle">创 建</el-button>
        <el-button type="primary" v-else @click="updateArticle">修 改</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        formInline: {
          host: '',
          port: '',
          user: '',
          password: '',
          database: '',
          excelName: ''
        },
        totalCount: 0, //分页组件--数据总条数
        list: [],//表格的数据
        listLoading: false,//数据加载等待动画
        listQuery: {
          pageNum: 1,//页码
          pageRow: 50,//每页条数
          name: ''
        },
        dialogStatus: 'create',
        dialogFormVisible: false,
        showPagination: false,
        textMap: {
          update: '修改键值',
          create: '新增键值'
        },
        tempArticle: {
          id: "",
          content: ""
        }
      }
    },
    created() {
      // this.getList();
    },
    methods: {
      getList() {
        //查询列表
        if (!this.hasPerm('redis:list')) {
          return
        }
        this.listLoading = true;
        this.api({
          url: "/redis/list",
          method: "get",
          params: this.listQuery
        }).then(data => {
          this.listLoading = false;
          this.list = data.list;
          this.totalCount = data.totalCount;
          this.showPagination = true;
        })
      },
      exportData() {
        console.log('exportData!');
      },
      handleSizeChange(val) {
        //改变每页数量
        this.listQuery.pageRow = val
        this.handleFilter();
      },
      handleCurrentChange(val) {
        //改变页码
        this.listQuery.pageNum = val
        this.getList();
      },
      handleFilter() {
        //改变了查询条件,从第一页开始查询
        this.listQuery.pageNum = 1
        this.getList()
      },
      getIndex($index) {
        //表格序号
        return (this.listQuery.pageNum - 1) * this.listQuery.pageRow + $index + 1
      },
      showCreate() {
        //显示新增对话框
        this.tempArticle.content = "";
        this.dialogStatus = "create"
        this.dialogFormVisible = true
      },
      showUpdate($index) {
        //显示修改对话框
        this.tempArticle.id = this.list[$index].id;
        this.tempArticle.content = this.list[$index].content;
        this.dialogStatus = "update"
        this.dialogFormVisible = true
      },
      createArticle() {
        //保存新文章
        this.api({
          url: "/redis/addArticle",
          method: "post",
          data: this.tempArticle
        }).then(() => {
          this.getList();
          this.dialogFormVisible = false
        })
      },
      updateArticle() {
        //修改文章
        this.api({
          url: "/redis/updateArticle",
          method: "post",
          data: this.tempArticle
        }).then(() => {
          this.getList();
          this.dialogFormVisible = false
        })
      },
    }
  }
</script>
