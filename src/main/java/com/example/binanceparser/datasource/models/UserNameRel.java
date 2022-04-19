package com.example.binanceparser.datasource.models;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserNameRel {
    @CsvBindByPosition(position = 0)
    private String user;
    @CsvBindByPosition(position = 1)
    private String name;

    //TODO було б круто, якби ми могли не ставити ці анотації на поля, а мати такий серіалізатор який би спочатку читав
    // назви колонок в csv і потім сам їх мапив на джава-поля.
    // я думаю таке існує. але якшо ні, то самі писати не будем
}
