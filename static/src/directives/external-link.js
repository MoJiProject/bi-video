// src/directives/external-link.js
export default {
    // 当绑定元素的父组件挂载时调用
    mounted(el) {
      const links = el.querySelectorAll('a[href^="http://"], a[href^="https://"]');
 // 选择所有外部链接
  
      links.forEach(link => {
        link.setAttribute('target', '_blank');
        link.setAttribute('rel', 'noopener noreferrer'); // 增加安全性
      });
    }
  }
  