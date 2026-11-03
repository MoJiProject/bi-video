const labelMap = {
  id: 'ID', userName: '用户名', phone: '手机号', avatarAddress: '头像地址',
  videoNumber: '投稿数', fansNumber: '粉丝数', likeNumber: '获赞数',
  createTime: '创建时间', loginDateTime: '登录时间', adminFlag: '管理员',
  banFlag: '封禁', banTime: '封禁时间', unbanTime: '解封时间',
  status: '状态', title: '标题', coverAddress: '封面', playNumber: '播放数',
  collectNumber: '收藏数', commentNumber: '评论数', userId: '用户ID',
  userAvatar: '用户头像', type: '类型', subZoneKey: '分区', subZoneValue: '分区值',
  examineFiledMessage: '审核信息', videoTime: '时长', word: '搜索词',
  count: '搜索次数', name: '名称', content: '内容', link: '链接',
  updateTime: '更新时间', operator: '操作人', time: '时间', module: '模块',
  action: '操作', target: '操作对象', description: '说明', ip: 'IP',
  sender: '发送人', receiver: '接收人', message: '消息内容',
  sendTime: '发送时间', dynamicType: '动态类型',
};

export function exportCsv(filename, rows, labels) {
  const data = Array.isArray(rows) ? rows : [];
  if (!data.length) return;
  const keys = Object.keys(data[0]);
  const header = Array.isArray(labels) && labels.length === keys.length
    ? labels
    : keys.map((k) => labelMap[k] || k);
  const escape = (v) => {
    if (v == null) return '';
    const s = String(v).replace(/"/g, '""');
    return /[",\n]/.test(s) ? `"${s}"` : s;
  };
  const lines = [header.join(',')];
  data.forEach((row) => {
    lines.push(keys.map((k) => escape(row[k])).join(','));
  });
  const csv = '\uFEFF' + lines.join('\r\n');
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${filename}.csv`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
