interface Category {
  id: number;
  name: string;
  subcategories: { id: number; name: string }[];
}

interface SelectedCategory {
  id: number;
  name: string;
}

export const useCategories = (): Ref<Category[]> => useState<Category[]>('categories', () => [
  {
    id: 1,
    name: 'Category 1',
    subcategories: [
      { id: 1, name: 'Subcategory 1-1' },
      { id: 2, name: 'Subcategory 1-2' }
    ]
  },
  {
    id: 2,
    name: 'Blood',
    subcategories: [
      { id: 3, name: 'Subcategory 2-1' },
      { id: 4, name: 'Subcategory 2-2' }
    ]
  },
  {
    id: 3,
    name: 'Category 3',
    subcategories: [
      { id: 5, name: 'Subcategory 3-1' },
      { id: 6, name: 'Subcategory 3-2' }
    ]
  },
  {
    id: 4,
    name: 'Category 4',
    subcategories: [
      { id: 7, name: 'Subcategory 4-1' },
      { id: 8, name: 'Subcategory 4-2' }
    ]
  },
  {
    id: 5,
    name: 'Category 5',
    subcategories: [
      { id: 9, name: 'Subcategory 5-1' },
      { id: 10, name: 'Subcategory 5-2' }
    ]
  },
  {
    id: 6,
    name: 'Category 6',
    subcategories: [
      { id: 11, name: 'Subcategory 6-1' },
      { id: 12, name: 'Subcategory 6-2' }
    ]
  },
  {
    id: 7,
    name: 'Category 7',
    subcategories: [
      { id: 13, name: 'Subcategory 7-1' },
      { id: 14, name: 'Subcategory 7-2' }
    ]
  },
  {
    id: 8,
    name: 'Category 8',
    subcategories: [
      { id: 15, name: 'Subcategory 8-1' },
      { id: 16, name: 'Subcategory 8-2' }
    ]
  },
  {
    id: 9,
    name: 'Category 9',
    subcategories: [
      { id: 17, name: 'Subcategory 9-1' },
      { id: 18, name: 'Subcategory 9-2' }
    ]
  },
  {
    id: 10,
    name: 'Category 10',
    subcategories: [
      { id: 19, name: 'Subcategory 10-1' },
      { id: 20, name: 'Subcategory 10-2' }
    ]
  },
  {
    id: 11,
    name: 'Category 11',
    subcategories: [
      { id: 21, name: 'Subcategory 11-1' },
      { id: 22, name: 'Subcategory 11-2' }
    ]
  },
  {
    id: 12,
    name: 'Category 12',
    subcategories: [
      { id: 23, name: 'Subcategory 12-1' },
      { id: 24, name: 'Subcategory 12-2' }
    ]
  },
  {
    id: 13,
    name: 'Category 13',
    subcategories: [
      { id: 25, name: 'Subcategory 13-1' },
      { id: 26, name: 'Subcategory 13-2' }
    ]
  },
  {
    id: 14,
    name: 'Category 14',
    subcategories: [
      { id: 27, name: 'Subcategory 14-1' },
      { id: 28, name: 'Subcategory 14-2' }
    ]
  },
  {
    id: 15,
    name: 'Category 15',
    subcategories: [
      { id: 29, name: 'Subcategory 15-1' },
      { id: 30, name: 'Subcategory 15-2' }
    ]
  }]);

  let currentCategory:()=>Ref<SelectedCategory|null> = ()=>useState<SelectedCategory|null>('currentCategory', () => null);

  export const selectedCategory = (idCategory: number, idSubcategory: number|null) => {
    let element:Category | undefined = useCategories().value.find(category => category.id === idCategory);
    if (element && !idSubcategory) {
      currentCategory().value = {
        id: element.id,
        name: element.name
      };
      return;
    }
    let subelement:SelectedCategory | undefined = element?.subcategories.find(subcategory => subcategory.id === idSubcategory);
    if (subelement) {
      currentCategory().value = subelement;
    }
  };

  export const getCurrentCategory:()=>Ref<SelectedCategory|null> = () => useState<SelectedCategory|null>('currentCategory', () => currentCategory().value);

  export const clearCategories = ()=>{
    currentCategory().value = null;
  }