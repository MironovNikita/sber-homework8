package org.example.service;

import java.util.ArrayList;
import java.util.List;

public class ServiceImpl implements Service {
    @Override
    public double powerNumber(double number, double degree) {
        return Math.pow(number, degree);
    }

    @Override
    public int factorial(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("Вычисление факториала отрицательного числа невозможно!");
        }
        int factorial = 1;
        for (int i = 2; i <= number; i++) {
            factorial *= i;
        }

        return factorial;
    }

    @Override
    public List<Integer> dividedWithoutRemainder(int number) {
        List<Integer> answer = new ArrayList<>();

        if (number > 0) {
            for (int i = 1; i <= number; i++) {
                if (number % i == 0) {
                    answer.add(i);
                }
            }
        } else if (number < 0) {
            for (int i = number; i < 0; i++) {
                if (number % i == 0) {
                    answer.add(i);
                }
            }
        } else answer.add(0);

        return answer;
    }

    @Override
    public int squaring(String word, int number) {
        return number * number;
    }
}
