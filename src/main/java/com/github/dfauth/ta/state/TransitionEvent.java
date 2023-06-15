package com.github.dfauth.ta.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class TransitionEvent<T,U,E> {
    private State<T,U,E> from;
    private State<T,U,E> to;
    private E event;
    private U ctx;
}
