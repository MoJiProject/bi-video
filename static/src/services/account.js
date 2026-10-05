/** 新后端还没有找回密码接口，这里明确报「未开放」，而不是打一个不存在的地址。 */
export async function forgetPassword() {
  throw new Error('找回密码功能暂未开放');
}