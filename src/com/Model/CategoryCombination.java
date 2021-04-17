package com.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Вспомогательный класс, позволяющий находить все уникальные неповторяющиеся сочетания категорий из определенного списка.
 */
public class CategoryCombination {

    public List<String[]> combos;

    public CategoryCombination(List<String> categoryNames) {
        combos = getInitialCategories(categoryNames);
    }

    /**
     * Получает список массивов, каждый из которых содержит название одной из категорий.
     *
     * @param categoryNames - список категорий
     * @return список массивов, по 1 на категорию
     */
    private List<String[]> getInitialCategories(List<String> categoryNames) {
        List<String[]> newCombos = new ArrayList<>();
        for(String categoryName: categoryNames) {
            String[] newCombo = {categoryName};
            newCombos.add(newCombo);
        }
        return newCombos;
    }

    /**
     * Добавляет в каждый из полученных списков категорий 1 новую категорию (категории внутри списка сортируются в
     * алфавитном порядке, чтобы избежать повторений); получает список всех комбинаций старых категорий с новой,
     * удовлетворяющих этому условию.
     *
     * @param categoryNames - список категорий
     */
    public void addCategory(List<String> categoryNames) {
        List<String[]> newCombos = new ArrayList<>();
        for(String[] combo: combos) {
            for(String categoryName: categoryNames) {
                if(categoryName.compareTo(combo[combo.length - 1]) <= 0) {
                    continue;
                }
                String[] newCombo = new String[combo.length + 1];
                System.arraycopy(combo, 0, newCombo, 0, combo.length);
                newCombo[newCombo.length - 1] = categoryName;
                newCombos.add(newCombo);
            }
        }
        combos = newCombos;
    }

}
