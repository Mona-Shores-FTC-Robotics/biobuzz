{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-shoot-r-1",
      "color": "#3cc8e4",
      "name": "START to SHOOT_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 18
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 82
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-w-2",
      "color": "#3cc8e4",
      "name": "START to SWEEP_W",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 18,
        "y": 10
      },
      "controlPoints": [
        {
          "x": 55,
          "y": 26
        },
        {
          "x": 24,
          "y": 26
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-1-3",
      "color": "#3cc8e4",
      "name": "SWEEP_W to SWEEP_1",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 37,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-shoot-r-4",
      "color": "#3cc8e4",
      "name": "SWEEP_1 to SHOOT_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 18
      },
      "controlPoints": [
        {
          "x": 37,
          "y": 20
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 82
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-2-5",
      "color": "#3cc8e4",
      "name": "SWEEP_1 to SWEEP_2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-shoot-r-6",
      "color": "#3cc8e4",
      "name": "SWEEP_2 to SHOOT_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 18
      },
      "controlPoints": [
        {
          "x": 41.5,
          "y": 20
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 82
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-3-7",
      "color": "#3cc8e4",
      "name": "SWEEP_2 to SWEEP_3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 46,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-shoot-r-8",
      "color": "#3cc8e4",
      "name": "SWEEP_3 to SHOOT_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 18
      },
      "controlPoints": [
        {
          "x": 46,
          "y": 20
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 82
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-4-9",
      "color": "#3cc8e4",
      "name": "SWEEP_3 to SWEEP_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-shoot-r-10",
      "color": "#3cc8e4",
      "name": "SWEEP_4 to SHOOT_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 18
      },
      "controlPoints": [
        {
          "x": 50.5,
          "y": 20
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 82
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-5-11",
      "color": "#3cc8e4",
      "name": "SWEEP_4 to SWEEP_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-shoot-r-12",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to SHOOT_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 18
      },
      "controlPoints": [
        {
          "x": 55,
          "y": 20
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 82
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-6-13",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to SWEEP_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-shoot-r-14",
      "color": "#3cc8e4",
      "name": "SWEEP_6 to SHOOT_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50,
        "y": 18
      },
      "controlPoints": [
        {
          "x": 55,
          "y": 20
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 82
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-garden-in-15",
      "color": "#3cc8e4",
      "name": "SHOOT_R to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 22
      },
      "controlPoints": [
        {
          "x": 52,
          "y": 26
        },
        {
          "x": 30,
          "y": 24
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 82
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 82,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-garden-16",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 11
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-wait-17",
      "color": "#3cc8e4",
      "name": "GARDEN to WAIT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40,
        "y": 16
      },
      "controlPoints": [
        {
          "x": 24,
          "y": 14
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 66
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 66
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-0-18",
      "color": "#3cc8e4",
      "name": "WAIT to SWEEP_0",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 66,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-1-19",
      "color": "#3cc8e4",
      "name": "SWEEP_0 to SWEEP_1",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 37,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-1-20",
      "color": "#3cc8e4",
      "name": "SWEEP_1 to HOLD_1",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 35,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 61.8
      }
    },
    {
      "id": "to-sweep-1-21",
      "color": "#3cc8e4",
      "name": "HOLD_1 to SWEEP_1",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 37,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 61.8,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-2-22",
      "color": "#3cc8e4",
      "name": "SWEEP_1 to SWEEP_2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-2-23",
      "color": "#3cc8e4",
      "name": "SWEEP_2 to HOLD_2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 39.5,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 66.6
      }
    },
    {
      "id": "to-sweep-3-24",
      "color": "#3cc8e4",
      "name": "SWEEP_2 to SWEEP_3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 46,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-3-25",
      "color": "#3cc8e4",
      "name": "SWEEP_3 to HOLD_3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 44,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 71.9
      }
    },
    {
      "id": "to-sweep-4-26",
      "color": "#3cc8e4",
      "name": "SWEEP_3 to SWEEP_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-4-27",
      "color": "#3cc8e4",
      "name": "SWEEP_4 to HOLD_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 48.5,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 77.5
      }
    },
    {
      "id": "to-sweep-5-28",
      "color": "#3cc8e4",
      "name": "SWEEP_4 to SWEEP_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-5-29",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to HOLD_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 53,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 83.3
      }
    },
    {
      "id": "to-sweep-6-30",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to SWEEP_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-6-31",
      "color": "#3cc8e4",
      "name": "SWEEP_6 to HOLD_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 86.0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-2-32",
      "color": "#3cc8e4",
      "name": "SWEEP_1 to SWEEP_2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-2-33",
      "color": "#3cc8e4",
      "name": "SWEEP_2 to HOLD_2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 39.5,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 66.6
      }
    },
    {
      "id": "to-sweep-2-34",
      "color": "#3cc8e4",
      "name": "HOLD_2 to SWEEP_2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 66.6,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-3-35",
      "color": "#3cc8e4",
      "name": "SWEEP_2 to SWEEP_3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 46,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-3-36",
      "color": "#3cc8e4",
      "name": "SWEEP_3 to HOLD_3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 44,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 71.9
      }
    },
    {
      "id": "to-sweep-4-37",
      "color": "#3cc8e4",
      "name": "SWEEP_3 to SWEEP_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-4-38",
      "color": "#3cc8e4",
      "name": "SWEEP_4 to HOLD_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 48.5,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 77.5
      }
    },
    {
      "id": "to-sweep-5-39",
      "color": "#3cc8e4",
      "name": "SWEEP_4 to SWEEP_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-5-40",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to HOLD_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 53,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 83.3
      }
    },
    {
      "id": "to-sweep-6-41",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to SWEEP_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-6-42",
      "color": "#3cc8e4",
      "name": "SWEEP_6 to HOLD_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 86.0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-3-43",
      "color": "#3cc8e4",
      "name": "SWEEP_2 to SWEEP_3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 46,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-3-44",
      "color": "#3cc8e4",
      "name": "SWEEP_3 to HOLD_3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 44,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 71.9
      }
    },
    {
      "id": "to-sweep-3-45",
      "color": "#3cc8e4",
      "name": "HOLD_3 to SWEEP_3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 46,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 71.9,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-4-46",
      "color": "#3cc8e4",
      "name": "SWEEP_3 to SWEEP_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-4-47",
      "color": "#3cc8e4",
      "name": "SWEEP_4 to HOLD_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 48.5,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 77.5
      }
    },
    {
      "id": "to-sweep-5-48",
      "color": "#3cc8e4",
      "name": "SWEEP_4 to SWEEP_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-5-49",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to HOLD_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 53,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 83.3
      }
    },
    {
      "id": "to-sweep-6-50",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to SWEEP_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-6-51",
      "color": "#3cc8e4",
      "name": "SWEEP_6 to HOLD_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 86.0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-4-52",
      "color": "#3cc8e4",
      "name": "SWEEP_3 to SWEEP_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-4-53",
      "color": "#3cc8e4",
      "name": "SWEEP_4 to HOLD_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 48.5,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 77.5
      }
    },
    {
      "id": "to-sweep-4-54",
      "color": "#3cc8e4",
      "name": "HOLD_4 to SWEEP_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 50.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 77.5,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-5-55",
      "color": "#3cc8e4",
      "name": "SWEEP_4 to SWEEP_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-5-56",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to HOLD_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 53,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 83.3
      }
    },
    {
      "id": "to-sweep-6-57",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to SWEEP_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-6-58",
      "color": "#3cc8e4",
      "name": "SWEEP_6 to HOLD_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 86.0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-5-59",
      "color": "#3cc8e4",
      "name": "SWEEP_4 to SWEEP_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-5-60",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to HOLD_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 53,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 83.3
      }
    },
    {
      "id": "to-sweep-5-61",
      "color": "#3cc8e4",
      "name": "HOLD_5 to SWEEP_5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 83.3,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-6-62",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to SWEEP_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-6-63",
      "color": "#3cc8e4",
      "name": "SWEEP_6 to HOLD_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 86.0
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-6-64",
      "color": "#3cc8e4",
      "name": "SWEEP_5 to SWEEP_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-hold-6-65",
      "color": "#3cc8e4",
      "name": "SWEEP_6 to HOLD_6",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 14
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 0,
                "endDeg": 86.0
              }
            }
          ]
        }
      }
    }
  ],
  "shapes": [
    {
      "id": "frame-leg-red",
      "name": "HIVE frame leg (red side)",
      "vertices": [
        {
          "x": 46.6,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 90.3
        },
        {
          "x": 46.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "frame-leg-blue",
      "name": "HIVE frame leg (blue side)",
      "vertices": [
        {
          "x": 92.6,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 90.3
        },
        {
          "x": 92.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "blue-half",
      "name": "Blue half: stay out",
      "vertices": [
        {
          "x": 70.8,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 141.5
        },
        {
          "x": 70.8,
          "y": 141.5
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "to-shoot-r-1"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-w-2"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-1-3"
    },
    {
      "kind": "path",
      "lineId": "to-shoot-r-4"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-2-5"
    },
    {
      "kind": "path",
      "lineId": "to-shoot-r-6"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-3-7"
    },
    {
      "kind": "path",
      "lineId": "to-shoot-r-8"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-4-9"
    },
    {
      "kind": "path",
      "lineId": "to-shoot-r-10"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-5-11"
    },
    {
      "kind": "path",
      "lineId": "to-shoot-r-12"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-6-13"
    },
    {
      "kind": "path",
      "lineId": "to-shoot-r-14"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-15"
    },
    {
      "kind": "path",
      "lineId": "to-garden-16"
    },
    {
      "kind": "path",
      "lineId": "to-wait-17"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-0-18"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-1-19"
    },
    {
      "kind": "path",
      "lineId": "to-hold-1-20"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-1-21"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-2-22"
    },
    {
      "kind": "path",
      "lineId": "to-hold-2-23"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-3-24"
    },
    {
      "kind": "path",
      "lineId": "to-hold-3-25"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-4-26"
    },
    {
      "kind": "path",
      "lineId": "to-hold-4-27"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-5-28"
    },
    {
      "kind": "path",
      "lineId": "to-hold-5-29"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-6-30"
    },
    {
      "kind": "path",
      "lineId": "to-hold-6-31"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-2-32"
    },
    {
      "kind": "path",
      "lineId": "to-hold-2-33"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-2-34"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-3-35"
    },
    {
      "kind": "path",
      "lineId": "to-hold-3-36"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-4-37"
    },
    {
      "kind": "path",
      "lineId": "to-hold-4-38"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-5-39"
    },
    {
      "kind": "path",
      "lineId": "to-hold-5-40"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-6-41"
    },
    {
      "kind": "path",
      "lineId": "to-hold-6-42"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-3-43"
    },
    {
      "kind": "path",
      "lineId": "to-hold-3-44"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-3-45"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-4-46"
    },
    {
      "kind": "path",
      "lineId": "to-hold-4-47"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-5-48"
    },
    {
      "kind": "path",
      "lineId": "to-hold-5-49"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-6-50"
    },
    {
      "kind": "path",
      "lineId": "to-hold-6-51"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-4-52"
    },
    {
      "kind": "path",
      "lineId": "to-hold-4-53"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-4-54"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-5-55"
    },
    {
      "kind": "path",
      "lineId": "to-hold-5-56"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-6-57"
    },
    {
      "kind": "path",
      "lineId": "to-hold-6-58"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-5-59"
    },
    {
      "kind": "path",
      "lineId": "to-hold-5-60"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-5-61"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-6-62"
    },
    {
      "kind": "path",
      "lineId": "to-hold-6-63"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-6-64"
    },
    {
      "kind": "path",
      "lineId": "to-hold-6-65"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 18,
    "rHeight": 18,
    "safetyMargin": 1,
    "maxVelocity": 50,
    "maxAcceleration": 45.0,
    "maxDeceleration": 45.0,
    "fieldMap": "biobuzz.webp",
    "robotImage": "/robot.png",
    "showGhostPaths": false,
    "showOnionLayers": false,
    "onionLayerSpacing": 3,
    "onionColor": "#dc2626",
    "onionNextPointOnly": false,
    "showHeadingArrow": false,
    "showCurrentTValue": false,
    "leftPanelWidth": 262,
    "rightPanelWidth": 714,
    "headingArrowLength": 50,
    "headingArrowColor": "#ffffff",
    "headingArrowThickness": 2,
    "pathOpacity": 1,
    "leftPanelMinWidth": 0,
    "rightPanelMinWidth": 0,
    "penToolMaxPaths": 8,
    "curveThroughMaxPoints": 4,
    "experimentalFeatures": {
      "optimize": false,
      "curveThrough": false
    }
  },
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "exportName": "recycle3-right",
    "registry": {
      "actions": [
        "LaunchAll",
        "CollectSeen"
      ],
      "conditions": [
        "Empty",
        "Tip",
        "IntakeFull",
        "RightCellUp",
        "LeftCellUp"
      ],
      "typicalS": {
        "LaunchAll": 2.0,
        "CollectSeen": 2.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59,
        9.5,
        90
      ],
      "GARDEN_IN": [
        8.5,
        22,
        270
      ],
      "GARDEN": [
        8.5,
        11,
        270
      ],
      "WAIT": [
        40,
        16,
        66
      ],
      "SWEEP_0": [
        30,
        10,
        0
      ],
      "SWEEP_1": [
        37,
        10,
        0
      ],
      "HOLD_1": [
        35,
        14,
        61.8
      ],
      "SWEEP_2": [
        41.5,
        10,
        0
      ],
      "HOLD_2": [
        39.5,
        14,
        66.6
      ],
      "SWEEP_3": [
        46,
        10,
        0
      ],
      "HOLD_3": [
        44,
        14,
        71.9
      ],
      "SWEEP_4": [
        50.5,
        10,
        0
      ],
      "HOLD_4": [
        48.5,
        14,
        77.5
      ],
      "SWEEP_5": [
        55,
        10,
        0
      ],
      "HOLD_5": [
        53,
        14,
        83.3
      ],
      "SWEEP_6": [
        59.5,
        10,
        0
      ],
      "HOLD_6": [
        55,
        14,
        86.0
      ],
      "SHOOT_R": [
        50,
        18,
        82
      ],
      "SWEEP_W": [
        18,
        10,
        0
      ]
    },
    "pathEnds": {
      "to-shoot-r-1": "SHOOT_R",
      "to-sweep-w-2": "SWEEP_W",
      "to-sweep-1-3": "SWEEP_1",
      "to-shoot-r-4": "SHOOT_R",
      "to-sweep-2-5": "SWEEP_2",
      "to-shoot-r-6": "SHOOT_R",
      "to-sweep-3-7": "SWEEP_3",
      "to-shoot-r-8": "SHOOT_R",
      "to-sweep-4-9": "SWEEP_4",
      "to-shoot-r-10": "SHOOT_R",
      "to-sweep-5-11": "SWEEP_5",
      "to-shoot-r-12": "SHOOT_R",
      "to-sweep-6-13": "SWEEP_6",
      "to-shoot-r-14": "SHOOT_R",
      "to-garden-in-15": "GARDEN_IN",
      "to-garden-16": "GARDEN",
      "to-wait-17": "WAIT",
      "to-sweep-0-18": "SWEEP_0",
      "to-sweep-1-19": "SWEEP_1",
      "to-hold-1-20": "HOLD_1",
      "to-sweep-1-21": "SWEEP_1",
      "to-sweep-2-22": "SWEEP_2",
      "to-hold-2-23": "HOLD_2",
      "to-sweep-3-24": "SWEEP_3",
      "to-hold-3-25": "HOLD_3",
      "to-sweep-4-26": "SWEEP_4",
      "to-hold-4-27": "HOLD_4",
      "to-sweep-5-28": "SWEEP_5",
      "to-hold-5-29": "HOLD_5",
      "to-sweep-6-30": "SWEEP_6",
      "to-hold-6-31": "HOLD_6",
      "to-sweep-2-32": "SWEEP_2",
      "to-hold-2-33": "HOLD_2",
      "to-sweep-2-34": "SWEEP_2",
      "to-sweep-3-35": "SWEEP_3",
      "to-hold-3-36": "HOLD_3",
      "to-sweep-4-37": "SWEEP_4",
      "to-hold-4-38": "HOLD_4",
      "to-sweep-5-39": "SWEEP_5",
      "to-hold-5-40": "HOLD_5",
      "to-sweep-6-41": "SWEEP_6",
      "to-hold-6-42": "HOLD_6",
      "to-sweep-3-43": "SWEEP_3",
      "to-hold-3-44": "HOLD_3",
      "to-sweep-3-45": "SWEEP_3",
      "to-sweep-4-46": "SWEEP_4",
      "to-hold-4-47": "HOLD_4",
      "to-sweep-5-48": "SWEEP_5",
      "to-hold-5-49": "HOLD_5",
      "to-sweep-6-50": "SWEEP_6",
      "to-hold-6-51": "HOLD_6",
      "to-sweep-4-52": "SWEEP_4",
      "to-hold-4-53": "HOLD_4",
      "to-sweep-4-54": "SWEEP_4",
      "to-sweep-5-55": "SWEEP_5",
      "to-hold-5-56": "HOLD_5",
      "to-sweep-6-57": "SWEEP_6",
      "to-hold-6-58": "HOLD_6",
      "to-sweep-5-59": "SWEEP_5",
      "to-hold-5-60": "HOLD_5",
      "to-sweep-5-61": "SWEEP_5",
      "to-sweep-6-62": "SWEEP_6",
      "to-hold-6-63": "HOLD_6",
      "to-sweep-6-64": "SWEEP_6",
      "to-hold-6-65": "HOLD_6"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "w-1",
        "kind": "firstOf",
        "label": "Fire the preloads (TIP 1)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 4000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-2",
        "kind": "firstOf",
        "label": "TIP 1?",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ]
      },
      {
        "id": "w-3",
        "kind": "firstOf",
        "label": "TIP 1: catch the spill",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-24",
        "kind": "firstOf",
        "label": "Caught 4?",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": [
              {
                "id": "p-4",
                "kind": "path",
                "lineId": "to-shoot-r-1",
                "park": false
              }
            ],
            "label": "Yes"
          },
          {
            "afterMs": 400,
            "cards": [
              {
                "id": "p-5",
                "kind": "path",
                "lineId": "to-sweep-w-2",
                "park": false
              },
              {
                "id": "p-6",
                "kind": "path",
                "lineId": "to-sweep-1-3",
                "park": false
              },
              {
                "id": "w-23",
                "kind": "firstOf",
                "label": "Top up along the wall (3) (1)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": [
                      {
                        "id": "p-7",
                        "kind": "path",
                        "lineId": "to-shoot-r-4",
                        "park": false
                      }
                    ],
                    "label": "Full"
                  },
                  {
                    "afterMs": 350,
                    "cards": [
                      {
                        "id": "p-8",
                        "kind": "path",
                        "lineId": "to-sweep-2-5",
                        "park": false
                      },
                      {
                        "id": "w-22",
                        "kind": "firstOf",
                        "label": "Top up along the wall (3) (2)",
                        "rows": [
                          {
                            "when": [
                              "IntakeFull"
                            ],
                            "cards": [
                              {
                                "id": "p-9",
                                "kind": "path",
                                "lineId": "to-shoot-r-6",
                                "park": false
                              }
                            ],
                            "label": "Full"
                          },
                          {
                            "afterMs": 350,
                            "cards": [
                              {
                                "id": "p-10",
                                "kind": "path",
                                "lineId": "to-sweep-3-7",
                                "park": false
                              },
                              {
                                "id": "w-21",
                                "kind": "firstOf",
                                "label": "Top up along the wall (3) (3)",
                                "rows": [
                                  {
                                    "when": [
                                      "IntakeFull"
                                    ],
                                    "cards": [
                                      {
                                        "id": "p-11",
                                        "kind": "path",
                                        "lineId": "to-shoot-r-8",
                                        "park": false
                                      }
                                    ],
                                    "label": "Full"
                                  },
                                  {
                                    "afterMs": 350,
                                    "cards": [
                                      {
                                        "id": "p-12",
                                        "kind": "path",
                                        "lineId": "to-sweep-4-9",
                                        "park": false
                                      },
                                      {
                                        "id": "w-20",
                                        "kind": "firstOf",
                                        "label": "Top up along the wall (3) (4)",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": [
                                              {
                                                "id": "p-13",
                                                "kind": "path",
                                                "lineId": "to-shoot-r-10",
                                                "park": false
                                              }
                                            ],
                                            "label": "Full"
                                          },
                                          {
                                            "afterMs": 350,
                                            "cards": [
                                              {
                                                "id": "p-14",
                                                "kind": "path",
                                                "lineId": "to-sweep-5-11",
                                                "park": false
                                              },
                                              {
                                                "id": "w-19",
                                                "kind": "firstOf",
                                                "label": "Top up along the wall (3) (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": [
                                                      {
                                                        "id": "p-15",
                                                        "kind": "path",
                                                        "lineId": "to-shoot-r-12",
                                                        "park": false
                                                      }
                                                    ],
                                                    "label": "Full"
                                                  },
                                                  {
                                                    "afterMs": 350,
                                                    "cards": [
                                                      {
                                                        "id": "p-16",
                                                        "kind": "path",
                                                        "lineId": "to-sweep-6-13",
                                                        "park": false
                                                      },
                                                      {
                                                        "id": "w-17",
                                                        "kind": "firstOf",
                                                        "label": "Top up along the wall (3) (6)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 500,
                                                            "cards": []
                                                          }
                                                        ]
                                                      },
                                                      {
                                                        "id": "p-18",
                                                        "kind": "path",
                                                        "lineId": "to-shoot-r-14",
                                                        "park": false
                                                      }
                                                    ],
                                                    "label": "Not yet"
                                                  }
                                                ]
                                              }
                                            ],
                                            "label": "Not yet"
                                          }
                                        ]
                                      }
                                    ],
                                    "label": "Not yet"
                                  }
                                ]
                              }
                            ],
                            "label": "Not yet"
                          }
                        ]
                      }
                    ],
                    "label": "Not yet"
                  }
                ]
              }
            ],
            "label": "No: sweep"
          }
        ]
      },
      {
        "id": "w-25",
        "kind": "firstOf",
        "label": "Our CELL up (3)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-26",
        "kind": "firstOf",
        "label": "Our CELL up (3) (2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-27",
        "kind": "firstOf",
        "label": "Our CELL up (3) (3)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-28",
        "kind": "firstOf",
        "label": "Our CELL up (3) (4)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-29",
        "kind": "firstOf",
        "label": "Our CELL up (3) (5)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-30",
        "kind": "firstOf",
        "label": "Our CELL up (3) (6)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-31",
        "kind": "firstOf",
        "label": "Our CELL up (3) (7)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-32",
        "kind": "firstOf",
        "label": "Our CELL up (3) (8)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-33",
        "kind": "firstOf",
        "label": "Our CELL up (3) (9)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-34",
        "kind": "firstOf",
        "label": "Our CELL up (3) (10)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-35",
        "kind": "firstOf",
        "label": "Our CELL up (3) (11)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-36",
        "kind": "firstOf",
        "label": "Our CELL up (3) (12)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-37",
        "kind": "firstOf",
        "label": "Our CELL up (3) (13)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-38",
        "kind": "firstOf",
        "label": "Our CELL up (3) (14)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-39",
        "kind": "firstOf",
        "label": "Fire the spill (3)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 600,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-40",
        "kind": "path",
        "lineId": "to-garden-in-15",
        "park": false
      },
      {
        "id": "p-41",
        "kind": "path",
        "lineId": "to-garden-16",
        "park": false
      },
      {
        "id": "w-42",
        "kind": "firstOf",
        "label": "The GARDEN",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ]
      },
      {
        "id": "p-43",
        "kind": "path",
        "lineId": "to-wait-17",
        "park": false
      },
      {
        "id": "w-44",
        "kind": "firstOf",
        "label": "Fire the GARDEN (TIP 3)",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-45",
        "kind": "firstOf",
        "label": "TIP 3's spill lands",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "p-46",
        "kind": "path",
        "lineId": "to-sweep-0-18",
        "park": false
      },
      {
        "id": "p-47",
        "kind": "path",
        "lineId": "to-sweep-1-19",
        "park": false
      },
      {
        "id": "w-317",
        "kind": "firstOf",
        "label": "Sweep the spills (5) (1)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": [
              {
                "id": "p-48",
                "kind": "path",
                "lineId": "to-hold-1-20",
                "park": false
              },
              {
                "id": "w-49",
                "kind": "firstOf",
                "label": "Our CELL up (5)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-50",
                "kind": "firstOf",
                "label": "Our CELL up (5) (2)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-51",
                "kind": "firstOf",
                "label": "Our CELL up (5) (3)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-52",
                "kind": "firstOf",
                "label": "Our CELL up (5) (4)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-53",
                "kind": "firstOf",
                "label": "Our CELL up (5) (5)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-54",
                "kind": "firstOf",
                "label": "Our CELL up (5) (6)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-55",
                "kind": "firstOf",
                "label": "Our CELL up (5) (7)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-56",
                "kind": "firstOf",
                "label": "Our CELL up (5) (8)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-57",
                "kind": "firstOf",
                "label": "Our CELL up (5) (9)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-58",
                "kind": "firstOf",
                "label": "Our CELL up (5) (10)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-59",
                "kind": "firstOf",
                "label": "Our CELL up (5) (11)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-60",
                "kind": "firstOf",
                "label": "Our CELL up (5) (12)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-61",
                "kind": "firstOf",
                "label": "Our CELL up (5) (13)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-62",
                "kind": "firstOf",
                "label": "Our CELL up (5) (14)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-63",
                "kind": "firstOf",
                "label": "Fire (5)",
                "rows": [
                  {
                    "when": [
                      "Empty"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 400,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              },
              {
                "id": "p-64",
                "kind": "path",
                "lineId": "to-sweep-1-21",
                "park": false
              },
              {
                "id": "p-65",
                "kind": "path",
                "lineId": "to-sweep-2-22",
                "park": false
              },
              {
                "id": "w-114",
                "kind": "firstOf",
                "label": "Sweep the rest (5) (2)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": [
                      {
                        "id": "p-66",
                        "kind": "path",
                        "lineId": "to-hold-2-23",
                        "park": false
                      },
                      {
                        "id": "w-67",
                        "kind": "firstOf",
                        "label": "Fire again (TIP 5)",
                        "rows": [
                          {
                            "when": [
                              "LeftCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1500,
                            "cards": []
                          }
                        ],
                        "alongside": "LaunchAll"
                      },
                      {
                        "id": "w-70",
                        "kind": "firstOf",
                        "label": "Tipped? (5)",
                        "rows": [
                          {
                            "when": [
                              "LeftCellUp"
                            ],
                            "cards": [],
                            "label": "Yes"
                          },
                          {
                            "afterMs": 50,
                            "cards": [
                              {
                                "id": "w-68",
                                "kind": "firstOf",
                                "label": "Pick up more (5.1)",
                                "rows": [
                                  {
                                    "when": [
                                      "IntakeFull"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1500,
                                    "cards": []
                                  }
                                ],
                                "alongside": "CollectSeen"
                              },
                              {
                                "id": "w-69",
                                "kind": "firstOf",
                                "label": "Fire once more (5.1)",
                                "rows": [
                                  {
                                    "when": [
                                      "LeftCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1200,
                                    "cards": []
                                  }
                                ],
                                "alongside": "LaunchAll"
                              }
                            ],
                            "label": "No: more"
                          }
                        ]
                      },
                      {
                        "id": "w-73",
                        "kind": "firstOf",
                        "label": "Tipped? (5)",
                        "rows": [
                          {
                            "when": [
                              "LeftCellUp"
                            ],
                            "cards": [],
                            "label": "Yes"
                          },
                          {
                            "afterMs": 50,
                            "cards": [
                              {
                                "id": "w-71",
                                "kind": "firstOf",
                                "label": "Pick up more (5.2)",
                                "rows": [
                                  {
                                    "when": [
                                      "IntakeFull"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1500,
                                    "cards": []
                                  }
                                ],
                                "alongside": "CollectSeen"
                              },
                              {
                                "id": "w-72",
                                "kind": "firstOf",
                                "label": "Fire once more (5.2)",
                                "rows": [
                                  {
                                    "when": [
                                      "LeftCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1200,
                                    "cards": []
                                  }
                                ],
                                "alongside": "LaunchAll"
                              }
                            ],
                            "label": "No: more"
                          }
                        ]
                      }
                    ],
                    "label": "Full"
                  },
                  {
                    "afterMs": 350,
                    "cards": [
                      {
                        "id": "p-74",
                        "kind": "path",
                        "lineId": "to-sweep-3-24",
                        "park": false
                      },
                      {
                        "id": "w-113",
                        "kind": "firstOf",
                        "label": "Sweep the rest (5) (3)",
                        "rows": [
                          {
                            "when": [
                              "IntakeFull"
                            ],
                            "cards": [
                              {
                                "id": "p-75",
                                "kind": "path",
                                "lineId": "to-hold-3-25",
                                "park": false
                              },
                              {
                                "id": "w-76",
                                "kind": "firstOf",
                                "label": "Fire again (TIP 5)",
                                "rows": [
                                  {
                                    "when": [
                                      "LeftCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1500,
                                    "cards": []
                                  }
                                ],
                                "alongside": "LaunchAll"
                              },
                              {
                                "id": "w-79",
                                "kind": "firstOf",
                                "label": "Tipped? (5)",
                                "rows": [
                                  {
                                    "when": [
                                      "LeftCellUp"
                                    ],
                                    "cards": [],
                                    "label": "Yes"
                                  },
                                  {
                                    "afterMs": 50,
                                    "cards": [
                                      {
                                        "id": "w-77",
                                        "kind": "firstOf",
                                        "label": "Pick up more (5.1)",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1500,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "CollectSeen"
                                      },
                                      {
                                        "id": "w-78",
                                        "kind": "firstOf",
                                        "label": "Fire once more (5.1)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1200,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "LaunchAll"
                                      }
                                    ],
                                    "label": "No: more"
                                  }
                                ]
                              },
                              {
                                "id": "w-82",
                                "kind": "firstOf",
                                "label": "Tipped? (5)",
                                "rows": [
                                  {
                                    "when": [
                                      "LeftCellUp"
                                    ],
                                    "cards": [],
                                    "label": "Yes"
                                  },
                                  {
                                    "afterMs": 50,
                                    "cards": [
                                      {
                                        "id": "w-80",
                                        "kind": "firstOf",
                                        "label": "Pick up more (5.2)",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1500,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "CollectSeen"
                                      },
                                      {
                                        "id": "w-81",
                                        "kind": "firstOf",
                                        "label": "Fire once more (5.2)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1200,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "LaunchAll"
                                      }
                                    ],
                                    "label": "No: more"
                                  }
                                ]
                              }
                            ],
                            "label": "Full"
                          },
                          {
                            "afterMs": 350,
                            "cards": [
                              {
                                "id": "p-83",
                                "kind": "path",
                                "lineId": "to-sweep-4-26",
                                "park": false
                              },
                              {
                                "id": "w-112",
                                "kind": "firstOf",
                                "label": "Sweep the rest (5) (4)",
                                "rows": [
                                  {
                                    "when": [
                                      "IntakeFull"
                                    ],
                                    "cards": [
                                      {
                                        "id": "p-84",
                                        "kind": "path",
                                        "lineId": "to-hold-4-27",
                                        "park": false
                                      },
                                      {
                                        "id": "w-85",
                                        "kind": "firstOf",
                                        "label": "Fire again (TIP 5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1500,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "LaunchAll"
                                      },
                                      {
                                        "id": "w-88",
                                        "kind": "firstOf",
                                        "label": "Tipped? (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": [],
                                            "label": "Yes"
                                          },
                                          {
                                            "afterMs": 50,
                                            "cards": [
                                              {
                                                "id": "w-86",
                                                "kind": "firstOf",
                                                "label": "Pick up more (5.1)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "CollectSeen"
                                              },
                                              {
                                                "id": "w-87",
                                                "kind": "firstOf",
                                                "label": "Fire once more (5.1)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1200,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              }
                                            ],
                                            "label": "No: more"
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-91",
                                        "kind": "firstOf",
                                        "label": "Tipped? (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": [],
                                            "label": "Yes"
                                          },
                                          {
                                            "afterMs": 50,
                                            "cards": [
                                              {
                                                "id": "w-89",
                                                "kind": "firstOf",
                                                "label": "Pick up more (5.2)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "CollectSeen"
                                              },
                                              {
                                                "id": "w-90",
                                                "kind": "firstOf",
                                                "label": "Fire once more (5.2)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1200,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              }
                                            ],
                                            "label": "No: more"
                                          }
                                        ]
                                      }
                                    ],
                                    "label": "Full"
                                  },
                                  {
                                    "afterMs": 350,
                                    "cards": [
                                      {
                                        "id": "p-92",
                                        "kind": "path",
                                        "lineId": "to-sweep-5-28",
                                        "park": false
                                      },
                                      {
                                        "id": "w-111",
                                        "kind": "firstOf",
                                        "label": "Sweep the rest (5) (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": [
                                              {
                                                "id": "p-93",
                                                "kind": "path",
                                                "lineId": "to-hold-5-29",
                                                "park": false
                                              },
                                              {
                                                "id": "w-94",
                                                "kind": "firstOf",
                                                "label": "Fire again (TIP 5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "w-97",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-95",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-96",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-100",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-98",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-99",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              }
                                            ],
                                            "label": "Full"
                                          },
                                          {
                                            "afterMs": 350,
                                            "cards": [
                                              {
                                                "id": "p-101",
                                                "kind": "path",
                                                "lineId": "to-sweep-6-30",
                                                "park": false
                                              },
                                              {
                                                "id": "w-102",
                                                "kind": "firstOf",
                                                "label": "Sweep the rest (5) (6)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 500,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "p-103",
                                                "kind": "path",
                                                "lineId": "to-hold-6-31",
                                                "park": false
                                              },
                                              {
                                                "id": "w-104",
                                                "kind": "firstOf",
                                                "label": "Fire again (TIP 5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "w-107",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-105",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-106",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-110",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-108",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-109",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              }
                                            ],
                                            "label": "Not yet"
                                          }
                                        ]
                                      }
                                    ],
                                    "label": "Not yet"
                                  }
                                ]
                              }
                            ],
                            "label": "Not yet"
                          }
                        ]
                      }
                    ],
                    "label": "Not yet"
                  }
                ]
              }
            ],
            "label": "Full"
          },
          {
            "afterMs": 350,
            "cards": [
              {
                "id": "p-115",
                "kind": "path",
                "lineId": "to-sweep-2-32",
                "park": false
              },
              {
                "id": "w-316",
                "kind": "firstOf",
                "label": "Sweep the spills (5) (2)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": [
                      {
                        "id": "p-116",
                        "kind": "path",
                        "lineId": "to-hold-2-33",
                        "park": false
                      },
                      {
                        "id": "w-117",
                        "kind": "firstOf",
                        "label": "Our CELL up (5)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-118",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (2)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-119",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (3)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-120",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (4)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-121",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (5)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-122",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (6)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-123",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (7)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-124",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (8)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-125",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (9)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-126",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (10)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-127",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (11)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-128",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (12)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-129",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (13)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-130",
                        "kind": "firstOf",
                        "label": "Our CELL up (5) (14)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1000,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "w-131",
                        "kind": "firstOf",
                        "label": "Fire (5)",
                        "rows": [
                          {
                            "when": [
                              "Empty"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 400,
                            "cards": []
                          }
                        ],
                        "alongside": "LaunchAll"
                      },
                      {
                        "id": "p-132",
                        "kind": "path",
                        "lineId": "to-sweep-2-34",
                        "park": false
                      },
                      {
                        "id": "p-133",
                        "kind": "path",
                        "lineId": "to-sweep-3-35",
                        "park": false
                      },
                      {
                        "id": "w-172",
                        "kind": "firstOf",
                        "label": "Sweep the rest (5) (3)",
                        "rows": [
                          {
                            "when": [
                              "IntakeFull"
                            ],
                            "cards": [
                              {
                                "id": "p-134",
                                "kind": "path",
                                "lineId": "to-hold-3-36",
                                "park": false
                              },
                              {
                                "id": "w-135",
                                "kind": "firstOf",
                                "label": "Fire again (TIP 5)",
                                "rows": [
                                  {
                                    "when": [
                                      "LeftCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1500,
                                    "cards": []
                                  }
                                ],
                                "alongside": "LaunchAll"
                              },
                              {
                                "id": "w-138",
                                "kind": "firstOf",
                                "label": "Tipped? (5)",
                                "rows": [
                                  {
                                    "when": [
                                      "LeftCellUp"
                                    ],
                                    "cards": [],
                                    "label": "Yes"
                                  },
                                  {
                                    "afterMs": 50,
                                    "cards": [
                                      {
                                        "id": "w-136",
                                        "kind": "firstOf",
                                        "label": "Pick up more (5.1)",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1500,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "CollectSeen"
                                      },
                                      {
                                        "id": "w-137",
                                        "kind": "firstOf",
                                        "label": "Fire once more (5.1)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1200,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "LaunchAll"
                                      }
                                    ],
                                    "label": "No: more"
                                  }
                                ]
                              },
                              {
                                "id": "w-141",
                                "kind": "firstOf",
                                "label": "Tipped? (5)",
                                "rows": [
                                  {
                                    "when": [
                                      "LeftCellUp"
                                    ],
                                    "cards": [],
                                    "label": "Yes"
                                  },
                                  {
                                    "afterMs": 50,
                                    "cards": [
                                      {
                                        "id": "w-139",
                                        "kind": "firstOf",
                                        "label": "Pick up more (5.2)",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1500,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "CollectSeen"
                                      },
                                      {
                                        "id": "w-140",
                                        "kind": "firstOf",
                                        "label": "Fire once more (5.2)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1200,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "LaunchAll"
                                      }
                                    ],
                                    "label": "No: more"
                                  }
                                ]
                              }
                            ],
                            "label": "Full"
                          },
                          {
                            "afterMs": 350,
                            "cards": [
                              {
                                "id": "p-142",
                                "kind": "path",
                                "lineId": "to-sweep-4-37",
                                "park": false
                              },
                              {
                                "id": "w-171",
                                "kind": "firstOf",
                                "label": "Sweep the rest (5) (4)",
                                "rows": [
                                  {
                                    "when": [
                                      "IntakeFull"
                                    ],
                                    "cards": [
                                      {
                                        "id": "p-143",
                                        "kind": "path",
                                        "lineId": "to-hold-4-38",
                                        "park": false
                                      },
                                      {
                                        "id": "w-144",
                                        "kind": "firstOf",
                                        "label": "Fire again (TIP 5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1500,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "LaunchAll"
                                      },
                                      {
                                        "id": "w-147",
                                        "kind": "firstOf",
                                        "label": "Tipped? (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": [],
                                            "label": "Yes"
                                          },
                                          {
                                            "afterMs": 50,
                                            "cards": [
                                              {
                                                "id": "w-145",
                                                "kind": "firstOf",
                                                "label": "Pick up more (5.1)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "CollectSeen"
                                              },
                                              {
                                                "id": "w-146",
                                                "kind": "firstOf",
                                                "label": "Fire once more (5.1)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1200,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              }
                                            ],
                                            "label": "No: more"
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-150",
                                        "kind": "firstOf",
                                        "label": "Tipped? (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": [],
                                            "label": "Yes"
                                          },
                                          {
                                            "afterMs": 50,
                                            "cards": [
                                              {
                                                "id": "w-148",
                                                "kind": "firstOf",
                                                "label": "Pick up more (5.2)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "CollectSeen"
                                              },
                                              {
                                                "id": "w-149",
                                                "kind": "firstOf",
                                                "label": "Fire once more (5.2)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1200,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              }
                                            ],
                                            "label": "No: more"
                                          }
                                        ]
                                      }
                                    ],
                                    "label": "Full"
                                  },
                                  {
                                    "afterMs": 350,
                                    "cards": [
                                      {
                                        "id": "p-151",
                                        "kind": "path",
                                        "lineId": "to-sweep-5-39",
                                        "park": false
                                      },
                                      {
                                        "id": "w-170",
                                        "kind": "firstOf",
                                        "label": "Sweep the rest (5) (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": [
                                              {
                                                "id": "p-152",
                                                "kind": "path",
                                                "lineId": "to-hold-5-40",
                                                "park": false
                                              },
                                              {
                                                "id": "w-153",
                                                "kind": "firstOf",
                                                "label": "Fire again (TIP 5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "w-156",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-154",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-155",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-159",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-157",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-158",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              }
                                            ],
                                            "label": "Full"
                                          },
                                          {
                                            "afterMs": 350,
                                            "cards": [
                                              {
                                                "id": "p-160",
                                                "kind": "path",
                                                "lineId": "to-sweep-6-41",
                                                "park": false
                                              },
                                              {
                                                "id": "w-161",
                                                "kind": "firstOf",
                                                "label": "Sweep the rest (5) (6)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 500,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "p-162",
                                                "kind": "path",
                                                "lineId": "to-hold-6-42",
                                                "park": false
                                              },
                                              {
                                                "id": "w-163",
                                                "kind": "firstOf",
                                                "label": "Fire again (TIP 5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "w-166",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-164",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-165",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-169",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-167",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-168",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              }
                                            ],
                                            "label": "Not yet"
                                          }
                                        ]
                                      }
                                    ],
                                    "label": "Not yet"
                                  }
                                ]
                              }
                            ],
                            "label": "Not yet"
                          }
                        ]
                      }
                    ],
                    "label": "Full"
                  },
                  {
                    "afterMs": 350,
                    "cards": [
                      {
                        "id": "p-173",
                        "kind": "path",
                        "lineId": "to-sweep-3-43",
                        "park": false
                      },
                      {
                        "id": "w-315",
                        "kind": "firstOf",
                        "label": "Sweep the spills (5) (3)",
                        "rows": [
                          {
                            "when": [
                              "IntakeFull"
                            ],
                            "cards": [
                              {
                                "id": "p-174",
                                "kind": "path",
                                "lineId": "to-hold-3-44",
                                "park": false
                              },
                              {
                                "id": "w-175",
                                "kind": "firstOf",
                                "label": "Our CELL up (5)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-176",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (2)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-177",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (3)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-178",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (4)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-179",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (5)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-180",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (6)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-181",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (7)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-182",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (8)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-183",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (9)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-184",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (10)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-185",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (11)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-186",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (12)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-187",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (13)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-188",
                                "kind": "firstOf",
                                "label": "Our CELL up (5) (14)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 1000,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "w-189",
                                "kind": "firstOf",
                                "label": "Fire (5)",
                                "rows": [
                                  {
                                    "when": [
                                      "Empty"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 400,
                                    "cards": []
                                  }
                                ],
                                "alongside": "LaunchAll"
                              },
                              {
                                "id": "p-190",
                                "kind": "path",
                                "lineId": "to-sweep-3-45",
                                "park": false
                              },
                              {
                                "id": "p-191",
                                "kind": "path",
                                "lineId": "to-sweep-4-46",
                                "park": false
                              },
                              {
                                "id": "w-220",
                                "kind": "firstOf",
                                "label": "Sweep the rest (5) (4)",
                                "rows": [
                                  {
                                    "when": [
                                      "IntakeFull"
                                    ],
                                    "cards": [
                                      {
                                        "id": "p-192",
                                        "kind": "path",
                                        "lineId": "to-hold-4-47",
                                        "park": false
                                      },
                                      {
                                        "id": "w-193",
                                        "kind": "firstOf",
                                        "label": "Fire again (TIP 5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1500,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "LaunchAll"
                                      },
                                      {
                                        "id": "w-196",
                                        "kind": "firstOf",
                                        "label": "Tipped? (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": [],
                                            "label": "Yes"
                                          },
                                          {
                                            "afterMs": 50,
                                            "cards": [
                                              {
                                                "id": "w-194",
                                                "kind": "firstOf",
                                                "label": "Pick up more (5.1)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "CollectSeen"
                                              },
                                              {
                                                "id": "w-195",
                                                "kind": "firstOf",
                                                "label": "Fire once more (5.1)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1200,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              }
                                            ],
                                            "label": "No: more"
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-199",
                                        "kind": "firstOf",
                                        "label": "Tipped? (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": [],
                                            "label": "Yes"
                                          },
                                          {
                                            "afterMs": 50,
                                            "cards": [
                                              {
                                                "id": "w-197",
                                                "kind": "firstOf",
                                                "label": "Pick up more (5.2)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "CollectSeen"
                                              },
                                              {
                                                "id": "w-198",
                                                "kind": "firstOf",
                                                "label": "Fire once more (5.2)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1200,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              }
                                            ],
                                            "label": "No: more"
                                          }
                                        ]
                                      }
                                    ],
                                    "label": "Full"
                                  },
                                  {
                                    "afterMs": 350,
                                    "cards": [
                                      {
                                        "id": "p-200",
                                        "kind": "path",
                                        "lineId": "to-sweep-5-48",
                                        "park": false
                                      },
                                      {
                                        "id": "w-219",
                                        "kind": "firstOf",
                                        "label": "Sweep the rest (5) (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": [
                                              {
                                                "id": "p-201",
                                                "kind": "path",
                                                "lineId": "to-hold-5-49",
                                                "park": false
                                              },
                                              {
                                                "id": "w-202",
                                                "kind": "firstOf",
                                                "label": "Fire again (TIP 5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "w-205",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-203",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-204",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-208",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-206",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-207",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              }
                                            ],
                                            "label": "Full"
                                          },
                                          {
                                            "afterMs": 350,
                                            "cards": [
                                              {
                                                "id": "p-209",
                                                "kind": "path",
                                                "lineId": "to-sweep-6-50",
                                                "park": false
                                              },
                                              {
                                                "id": "w-210",
                                                "kind": "firstOf",
                                                "label": "Sweep the rest (5) (6)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 500,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "p-211",
                                                "kind": "path",
                                                "lineId": "to-hold-6-51",
                                                "park": false
                                              },
                                              {
                                                "id": "w-212",
                                                "kind": "firstOf",
                                                "label": "Fire again (TIP 5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "w-215",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-213",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-214",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-218",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-216",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-217",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              }
                                            ],
                                            "label": "Not yet"
                                          }
                                        ]
                                      }
                                    ],
                                    "label": "Not yet"
                                  }
                                ]
                              }
                            ],
                            "label": "Full"
                          },
                          {
                            "afterMs": 350,
                            "cards": [
                              {
                                "id": "p-221",
                                "kind": "path",
                                "lineId": "to-sweep-4-52",
                                "park": false
                              },
                              {
                                "id": "w-314",
                                "kind": "firstOf",
                                "label": "Sweep the spills (5) (4)",
                                "rows": [
                                  {
                                    "when": [
                                      "IntakeFull"
                                    ],
                                    "cards": [
                                      {
                                        "id": "p-222",
                                        "kind": "path",
                                        "lineId": "to-hold-4-53",
                                        "park": false
                                      },
                                      {
                                        "id": "w-223",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-224",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (2)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-225",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (3)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-226",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (4)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-227",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-228",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (6)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-229",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (7)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-230",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (8)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-231",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (9)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-232",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (10)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-233",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (11)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-234",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (12)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-235",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (13)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-236",
                                        "kind": "firstOf",
                                        "label": "Our CELL up (5) (14)",
                                        "rows": [
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1000,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "w-237",
                                        "kind": "firstOf",
                                        "label": "Fire (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "Empty"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 400,
                                            "cards": []
                                          }
                                        ],
                                        "alongside": "LaunchAll"
                                      },
                                      {
                                        "id": "p-238",
                                        "kind": "path",
                                        "lineId": "to-sweep-4-54",
                                        "park": false
                                      },
                                      {
                                        "id": "p-239",
                                        "kind": "path",
                                        "lineId": "to-sweep-5-55",
                                        "park": false
                                      },
                                      {
                                        "id": "w-258",
                                        "kind": "firstOf",
                                        "label": "Sweep the rest (5) (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": [
                                              {
                                                "id": "p-240",
                                                "kind": "path",
                                                "lineId": "to-hold-5-56",
                                                "park": false
                                              },
                                              {
                                                "id": "w-241",
                                                "kind": "firstOf",
                                                "label": "Fire again (TIP 5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "w-244",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-242",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-243",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-247",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-245",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-246",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              }
                                            ],
                                            "label": "Full"
                                          },
                                          {
                                            "afterMs": 350,
                                            "cards": [
                                              {
                                                "id": "p-248",
                                                "kind": "path",
                                                "lineId": "to-sweep-6-57",
                                                "park": false
                                              },
                                              {
                                                "id": "w-249",
                                                "kind": "firstOf",
                                                "label": "Sweep the rest (5) (6)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 500,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "p-250",
                                                "kind": "path",
                                                "lineId": "to-hold-6-58",
                                                "park": false
                                              },
                                              {
                                                "id": "w-251",
                                                "kind": "firstOf",
                                                "label": "Fire again (TIP 5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "w-254",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-252",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-253",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-257",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-255",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-256",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              }
                                            ],
                                            "label": "Not yet"
                                          }
                                        ]
                                      }
                                    ],
                                    "label": "Full"
                                  },
                                  {
                                    "afterMs": 350,
                                    "cards": [
                                      {
                                        "id": "p-259",
                                        "kind": "path",
                                        "lineId": "to-sweep-5-59",
                                        "park": false
                                      },
                                      {
                                        "id": "w-313",
                                        "kind": "firstOf",
                                        "label": "Sweep the spills (5) (5)",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": [
                                              {
                                                "id": "p-260",
                                                "kind": "path",
                                                "lineId": "to-hold-5-60",
                                                "park": false
                                              },
                                              {
                                                "id": "w-261",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-262",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (2)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-263",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (3)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-264",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (4)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-265",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-266",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (6)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-267",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (7)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-268",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (8)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-269",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (9)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-270",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (10)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-271",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (11)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-272",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (12)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-273",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (13)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-274",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (14)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-275",
                                                "kind": "firstOf",
                                                "label": "Fire (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "Empty"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 400,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "p-276",
                                                "kind": "path",
                                                "lineId": "to-sweep-5-61",
                                                "park": false
                                              },
                                              {
                                                "id": "p-277",
                                                "kind": "path",
                                                "lineId": "to-sweep-6-62",
                                                "park": false
                                              },
                                              {
                                                "id": "w-278",
                                                "kind": "firstOf",
                                                "label": "Sweep the rest (5) (6)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 500,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "p-279",
                                                "kind": "path",
                                                "lineId": "to-hold-6-63",
                                                "park": false
                                              },
                                              {
                                                "id": "w-280",
                                                "kind": "firstOf",
                                                "label": "Fire again (TIP 5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1500,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "w-283",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-281",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-282",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-286",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-284",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-285",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              }
                                            ],
                                            "label": "Full"
                                          },
                                          {
                                            "afterMs": 350,
                                            "cards": [
                                              {
                                                "id": "p-287",
                                                "kind": "path",
                                                "lineId": "to-sweep-6-64",
                                                "park": false
                                              },
                                              {
                                                "id": "w-288",
                                                "kind": "firstOf",
                                                "label": "Sweep the spills (5) (6)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 500,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "p-289",
                                                "kind": "path",
                                                "lineId": "to-hold-6-65",
                                                "park": false
                                              },
                                              {
                                                "id": "w-290",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-291",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (2)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-292",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (3)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-293",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (4)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-294",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-295",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (6)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-296",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (7)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-297",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (8)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-298",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (9)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-299",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (10)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-300",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (11)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-301",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (12)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-302",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (13)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-303",
                                                "kind": "firstOf",
                                                "label": "Our CELL up (5) (14)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "RightCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1000,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-304",
                                                "kind": "firstOf",
                                                "label": "Fire (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "Empty"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 400,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "w-305",
                                                "kind": "firstOf",
                                                "label": "Pick up the rest (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "IntakeFull"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1800,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "CollectSeen"
                                              },
                                              {
                                                "id": "w-306",
                                                "kind": "firstOf",
                                                "label": "Fire again (TIP 5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": []
                                                  },
                                                  {
                                                    "afterMs": 1200,
                                                    "cards": []
                                                  }
                                                ],
                                                "alongside": "LaunchAll"
                                              },
                                              {
                                                "id": "w-309",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-307",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-308",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.1)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "w-312",
                                                "kind": "firstOf",
                                                "label": "Tipped? (5)",
                                                "rows": [
                                                  {
                                                    "when": [
                                                      "LeftCellUp"
                                                    ],
                                                    "cards": [],
                                                    "label": "Yes"
                                                  },
                                                  {
                                                    "afterMs": 50,
                                                    "cards": [
                                                      {
                                                        "id": "w-310",
                                                        "kind": "firstOf",
                                                        "label": "Pick up more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "IntakeFull"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1500,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "CollectSeen"
                                                      },
                                                      {
                                                        "id": "w-311",
                                                        "kind": "firstOf",
                                                        "label": "Fire once more (5.2)",
                                                        "rows": [
                                                          {
                                                            "when": [
                                                              "LeftCellUp"
                                                            ],
                                                            "cards": []
                                                          },
                                                          {
                                                            "afterMs": 1200,
                                                            "cards": []
                                                          }
                                                        ],
                                                        "alongside": "LaunchAll"
                                                      }
                                                    ],
                                                    "label": "No: more"
                                                  }
                                                ]
                                              }
                                            ],
                                            "label": "Not yet"
                                          }
                                        ]
                                      }
                                    ],
                                    "label": "Not yet"
                                  }
                                ]
                              }
                            ],
                            "label": "Not yet"
                          }
                        ]
                      }
                    ],
                    "label": "Not yet"
                  }
                ]
              }
            ],
            "label": "Not yet"
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}