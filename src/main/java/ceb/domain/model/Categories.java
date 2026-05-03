package ceb.domain.model;
public class Categories {
    private Integer CategoryId;
    private String CategoryName;
    private String Description;
    private String Icon;

    public Categories() {}

    public Categories(Integer categoryId, String categoryName, String description, String icon) {
        CategoryId = categoryId;
        CategoryName = categoryName;
        Description = description;
        Icon = icon;
    }

    public Integer getCategoryId() { return CategoryId; }
    public void setCategoryId(Integer categoryId) { CategoryId = categoryId; }

    public String getCategoryName() { return CategoryName; }
    public void setCategoryName(String categoryName) { CategoryName = categoryName; }

    public String getDescription() { return Description; }
    public void setDescription(String description) { Description = description; }

    public String getIcon() { return Icon; }
    public void setIcon(String icon) { Icon = icon; }
}
