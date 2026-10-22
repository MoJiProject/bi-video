const CATEGORY_LABELS = { "番剧": "动画剧场", "国创": "原创动画" };

export function formatContentCategories(categories) {
  return categories.map(category => ({
    id: category.id,
    label: CATEGORY_LABELS[category.name] || category.name,
    value: category.name,
  }));
}
