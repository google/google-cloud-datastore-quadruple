//  Copyright 2025 Google LLC
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      https://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

import 'jasmine';
import {Quadruple} from '../quadruple';

/** Convert a quadruple to IEEE 64-bit floating point. For tests only,
 *  does not round bit 52 of the mantissa.
 */
function toNumber(q: Quadruple): number {
  if (q.biasedExponent == 0) {
    return q.negative ? -0.0 : 0.0;
  }
  if (q.biasedExponent == 0xffffffff) {
    return (q.mantHi | q.mantLo) != 0n
      ? NaN
      : q.negative
        ? -Infinity
        : Infinity;
  }
  let doubleExponent = q.exponent();
  let mantissa = q.mantHi >> 12n; // Does not round.
  if (doubleExponent > 1023) {
    doubleExponent = 1024;
    mantissa = 0n;
  } else if (doubleExponent < -1022) {
    // subnormal
    if (doubleExponent < -1074) {
      mantissa = 0n;
    } else {
      mantissa = (mantissa | (1n << 52n)) >> BigInt(-doubleExponent - 1022);
    }
    doubleExponent = -1023;
  }
  return makeRawDouble(q.negative, doubleExponent, mantissa);
}

function makeDouble(negative: boolean, exponent: number, mantissa: bigint) {
  if (exponent < -1022) {
    mantissa = (mantissa | (1n << 53n)) >> BigInt(-exponent - 1022);
    exponent = -1023;
  }
  return makeRawDouble(negative, exponent, mantissa);
}

function makeRawDouble(negative: boolean, exponent: number, mantissa: bigint) {
  let bytes = new DataView(new ArrayBuffer(8));
  let signBits = negative ? 1n << 63n : 0n;
  let exponentBits = BigInt(exponent + 1023) << 52n;
  bytes.setBigUint64(0, signBits | exponentBits | mantissa);
  return bytes.getFloat64(0);
}

function random(max: number) {
  return Math.floor(max * Math.random());
}

function expectLessThan(q1: Quadruple, q2: Quadruple): void {
  expect(q1.compareTo(q2)).toBeLessThan(0);
}

function expectEquals(q1: Quadruple, q2: Quadruple): void {
  expect(q1.compareTo(q2))
    .withContext(q1.debug() + ' = ' + q2.debug())
    .toBe(0);
}

const otherNan = new Quadruple(true, 0xffffffff, 1n, 1n);
const quadrupleBias = 0x7fff_ffff;

describe('QuadrupleTest', function () {
  it('simple', function () {
    let q1 = Quadruple.fromNumber(1);
    let q2 = Quadruple.fromNumber(1.1);

    expect(toNumber(q1)).toBe(1.0);
    expect(toNumber(q2)).toBe(1.1);
    expectLessThan(q1, q2);
  });

  it('orderAndEquivalence', function () {
    let orderedEquivalences = [
      [
        Quadruple.negativeInfinity,
        Quadruple.fromNumber(-Infinity),
        Quadruple.fromString('-Infinity'),
      ],
      [Quadruple.fromNumber(-1.125), Quadruple.fromString('-1.125')],
      [Quadruple.fromNumber(-1), Quadruple.fromString('-1')],
      [
        Quadruple.fromString('-0'),
        Quadruple.fromNumber(-0.0),
        Quadruple.negativeZero,
      ],
      [
        Quadruple.fromString('+0'),
        Quadruple.fromString('0'),
        Quadruple.fromNumber(0.0),
        Quadruple.positiveZero,
      ],
      [Quadruple.fromNumber(325), Quadruple.fromString('325')],
      [
        Quadruple.positiveInfinity,
        Quadruple.fromNumber(Infinity),
        Quadruple.fromString('Infinity'),
        Quadruple.fromString('+Infinity'),
      ],
      [Quadruple.NaN, otherNan, Quadruple.fromString('NaN')],
    ];

    let classCount = orderedEquivalences.length;
    for (let classIndex1 = 0; classIndex1 < classCount; classIndex1++) {
      let class1 = orderedEquivalences[classIndex1];
      for (let classIndex2 = 0; classIndex2 < classCount; classIndex2++) {
        let class2 = orderedEquivalences[classIndex2];

        for (let index1 = 0; index1 < class1.length; index1++) {
          let q1 = class1[index1];
          for (let index2 = 0; index2 < class2.length; index2++) {
            let q2 = class2[index2];
            let testCase =
              classIndex1 + '/' + index1 + ' <-> ' + classIndex2 + '/' + index2;
            if (classIndex1 == classIndex2) {
              expect(q1.compareTo(q2)).withContext(testCase).toBe(0);
            } else if (classIndex1 < classIndex2) {
              expect(q1.compareTo(q2)).withContext(testCase).toBeLessThan(0);
              expect(q2.compareTo(q1)).withContext(testCase).toBeGreaterThan(0);
            } else {
              expect(q1.compareTo(q2)).withContext(testCase).toBeGreaterThan(0);
              expect(q2.compareTo(q1)).withContext(testCase).toBeLessThan(0);
            }
          }
        }
      }
    }
  });

  it('infinityAndNaN', function () {
    expect(toNumber(Quadruple.negativeInfinity)).toBe(-Infinity);
    expect(toNumber(Quadruple.positiveInfinity)).toBe(Infinity);

    expect(toNumber(Quadruple.NaN)).toBeNaN();
    expect(toNumber(otherNan)).toBeNaN();

    expect(Quadruple.NaN.isNaN()).toBeTrue();
    expect(otherNan.isNaN()).toBeTrue();
    expect(Quadruple.negativeInfinity.isNaN()).toBeFalse();
    expect(Quadruple.positiveZero.isNaN()).toBeFalse();
    expect(Quadruple.fromNumber(1).isNaN()).toBeFalse();

    expect(Quadruple.negativeInfinity.isInfinite()).toBeTrue();
    expect(Quadruple.positiveInfinity.isInfinite()).toBeTrue();
    expect(Quadruple.NaN.isInfinite()).toBeFalse();
    expect(Quadruple.positiveZero.isInfinite()).toBeFalse();
    expect(Quadruple.fromNumber(1).isInfinite()).toBeFalse();
  });

  it('zero', function () {
    expect(1 / toNumber(Quadruple.positiveZero)).toBePositiveInfinity();
    expect(1 / toNumber(Quadruple.negativeZero)).toBeNegativeInfinity();

    expect(Quadruple.negativeZero.isZero()).toBeTrue();
    expect(Quadruple.positiveZero.isZero()).toBeTrue();
    expect(Quadruple.negativeInfinity.isZero()).toBeFalse();
    expect(Quadruple.positiveInfinity.isZero()).toBeFalse();
    expect(Quadruple.NaN.isZero()).toBeFalse();
    expect(otherNan.isZero()).toBeFalse();
    expect(Quadruple.fromNumber(1).isZero()).toBeFalse();
  });

  it('randomQuadruple', function () {
    for (let i = 0; i < 1000; i++) {
      let mantissa = BigInt(random(0x10_0000_0000_0000)); // 52 bits
      // Avoid subnormal exponents, as it makes building identical double
      // and Quadruple values harder.
      let exponent = random(1022 + 1023 + 2) - 1022;
      let negative = random(2) == 0;
      let biasedExponent = exponent + quadrupleBias;
      let q = new Quadruple(negative, biasedExponent, mantissa << 12n, 0n);
      let d = makeDouble(negative, exponent, mantissa);
      expectEquals(q, Quadruple.fromNumber(d));

      // Make some nearby numbers.
      let qPlus1 = new Quadruple(negative, biasedExponent, mantissa << 12n, 1n);
      let qMinus1 = new Quadruple(
        negative,
        biasedExponent,
        BigInt.asUintN(64, (mantissa << 12n) - 1n),
        BigInt.asUintN(64, -1n),
      );
      let qMinus2 = new Quadruple(
        negative,
        biasedExponent,
        BigInt.asUintN(64, (mantissa << 12n) - 1n),
        BigInt.asUintN(64, -2n),
      );
      if (negative) {
        expectLessThan(qMinus1, qMinus2);
        expectLessThan(q, qMinus2);
        expectLessThan(qPlus1, qMinus2);
        expectLessThan(q, qMinus1);
        expectLessThan(qPlus1, qMinus1);
        expectLessThan(qPlus1, q);
      } else {
        expectLessThan(qMinus2, qMinus1);
        expectLessThan(qMinus2, q);
        expectLessThan(qMinus2, qPlus1);
        expectLessThan(qMinus1, q);
        expectLessThan(qMinus1, qPlus1);
        expectLessThan(q, qPlus1);
      }
    }
  });

  it('fromString_invalid', function () {
    invalidString('');
    invalidString('-NaN');
    invalidString('+NaN');
    invalidString('-');
    invalidString('+');
    invalidString('blurb');
    invalidString('1a');
    invalidString('123.45a');
    invalidString('123.45e');
    invalidString('123.45ek');
    invalidString('123.45e-k');
    invalidString('123.45e+k');
    invalidString('123.45e-12z');
    invalidString('123.45e+12z');
    invalidString('123.45e1234567890');
  });

  function invalidString(s: string) {
    expect(() => Quadruple.fromString(s)).toThrow();
  }

  it('fromString', function () {
    expectEquals(Quadruple.fromNumber(2), Quadruple.fromString('2'));
    expectEquals(Quadruple.fromNumber(22), Quadruple.fromString('+22'));
    expectEquals(Quadruple.fromNumber(-25), Quadruple.fromString('-25'));
    expectEquals(Quadruple.fromNumber(1.5), Quadruple.fromString('1.5'));
    expectEquals(Quadruple.fromNumber(0.5), Quadruple.fromString('.5'));
    expectEquals(Quadruple.fromNumber(12), Quadruple.fromString('12.'));
    expectEquals(Quadruple.fromNumber(420), Quadruple.fromString('42e1'));
    expectEquals(Quadruple.fromNumber(4200), Quadruple.fromString('4.2e3'));
    expectEquals(Quadruple.fromNumber(4100), Quadruple.fromString('4.1e+3'));
    expectEquals(Quadruple.fromNumber(42), Quadruple.fromString('420e-1'));
    expectEquals(
      Quadruple.fromNumber(100),
      Quadruple.fromString('1000000000000e-10'),
    );
  });
});
